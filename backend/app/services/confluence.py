from __future__ import annotations

import base64
import json
import re
from dataclasses import dataclass
from html.parser import HTMLParser
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

from ..core.config import settings


class ConfluenceError(RuntimeError):
    pass


@dataclass(frozen=True)
class ConfluencePage:
    title: str
    space_key: str
    url: str
    text: str


class _HtmlTextExtractor(HTMLParser):
    block_tags = {
        "br",
        "div",
        "h1",
        "h2",
        "h3",
        "h4",
        "h5",
        "h6",
        "li",
        "p",
        "tr",
    }

    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self._parts: list[str] = []

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        if tag.lower() in self.block_tags:
            self._parts.append("\n")

    def handle_endtag(self, tag: str) -> None:
        if tag.lower() in self.block_tags:
            self._parts.append("\n")

    def handle_data(self, data: str) -> None:
        stripped = data.strip()
        if stripped:
            self._parts.append(stripped)

    def text(self) -> str:
        raw_text = " ".join(self._parts)
        raw_text = re.sub(r"[ \t\r\f\v]+", " ", raw_text)
        raw_text = re.sub(r"\n\s*\n+", "\n", raw_text)
        return raw_text.strip()


class ConfluenceClient:
    def __init__(self) -> None:
        self._base_url = settings.confluence_base_url.rstrip("/")
        self._username = settings.confluence_username
        self._password = settings.confluence_password

    def fetch_pages(self) -> list[ConfluencePage]:
        if not self._username or not self._password:
            raise ConfluenceError(
                "Confluence credentials are not configured. "
                "Set CONFLUENCE_USERNAME and CONFLUENCE_PASSWORD in backend/.env."
            )

        pages: list[ConfluencePage] = []
        start = 0

        while len(pages) < settings.confluence_page_limit:
            batch_limit = min(25, settings.confluence_page_limit - len(pages))
            payload = self._request_content(start=start, limit=batch_limit)
            results = payload.get("results", [])
            if not results:
                break

            pages.extend(self._parse_page(item) for item in results)
            start += len(results)

            if len(results) < batch_limit:
                break

        return [page for page in pages if page.text]

    def _request_content(self, *, start: int, limit: int) -> dict[str, Any]:
        params = {
            "type": "page",
            "status": "current",
            "start": str(start),
            "limit": str(limit),
            "expand": "body.storage,space,version",
        }
        if settings.confluence_space_key:
            params["spaceKey"] = settings.confluence_space_key

        url = f"{self._base_url}/rest/api/content?{urlencode(params)}"
        request = Request(
            url,
            headers={
                "Accept": "application/json",
                "Authorization": self._basic_auth_header(),
            },
        )

        try:
            with urlopen(
                request,
                timeout=settings.confluence_request_timeout_seconds,
            ) as response:
                return json.loads(response.read().decode("utf-8"))
        except HTTPError as exc:
            detail = exc.read().decode("utf-8", errors="ignore")
            raise ConfluenceError(
                f"Confluence returned HTTP {exc.code}: {detail[:300]}"
            ) from exc
        except URLError as exc:
            raise ConfluenceError(f"Confluence request failed: {exc.reason}") from exc
        except json.JSONDecodeError as exc:
            raise ConfluenceError("Confluence returned invalid JSON.") from exc

    def _parse_page(self, item: dict[str, Any]) -> ConfluencePage:
        title = str(item.get("title") or "Untitled")
        space = item.get("space") or {}
        space_key = str(space.get("key") or "")
        body = item.get("body") or {}
        storage = body.get("storage") or {}
        html = str(storage.get("value") or "")
        webui = ((item.get("_links") or {}).get("webui")) or ""

        return ConfluencePage(
            title=title,
            space_key=space_key,
            url=f"{self._base_url}{webui}" if webui else self._base_url,
            text=self._html_to_text(html),
        )

    def _basic_auth_header(self) -> str:
        token = f"{self._username}:{self._password}".encode("utf-8")
        encoded = base64.b64encode(token).decode("ascii")
        return f"Basic {encoded}"

    @staticmethod
    def _html_to_text(html: str) -> str:
        parser = _HtmlTextExtractor()
        parser.feed(html)
        return parser.text()


def build_confluence_corpus() -> str:
    pages = ConfluenceClient().fetch_pages()
    if not pages:
        raise ConfluenceError("Confluence returned no readable pages.")

    parts = []
    for index, page in enumerate(pages, start=1):
        parts.append(
            "\n".join(
                [
                    f"# Confluence: {page.title}",
                    f"Space: {page.space_key}",
                    f"URL: {page.url}",
                    "",
                    page.text,
                ]
            )
        )

    return "\n\n".join(parts)
