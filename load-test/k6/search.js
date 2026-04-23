import http from "k6/http";
import { check, sleep } from "k6";
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/latest/dist/bundle.js";

export const options = {
    vus: Number(__ENV.VUS || 1),
    duration: __ENV.DURATION || "30s",
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{name:homepage}": ["p(95)<5000"],
        "http_req_duration{name:flight_search}": ["p(95)<5000"],
    },
};

const BROWSER_HEADERS = {
    "User-Agent":
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
    "Accept-Language": "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7",
};

function reqParams(stepName) {
    return {
        redirects: 5,
        tags: { name: stepName },
        headers: BROWSER_HEADERS,
    };
}

function buildDefaultSearchUrl(baseUrl) {
    const trimmed = baseUrl.replace(/\/+$/, "");

    const d = new Date();
    d.setDate(d.getDate() + 3);
    const pad = (n) => String(n).padStart(2, "0");
    const formattedDate = `${pad(d.getDate())}.${pad(d.getMonth() + 1)}.${d.getFullYear()}`;

    return (
        trimmed +
        "/ucak-bileti/arama/istanbul-ankara-esenboga-havalimani-ista-esb/" +
        `?gidis=${formattedDate}&yetiskin=1&sinif=ekonomi&currency=TRY&ref=homepage&geotrip=domestic&trip=domestic`
    );
}

export default function () {
    const baseUrl = (__ENV.BASE_URL || "https://www.enuygun.com").replace(/\/+$/, "");
    const searchUrl = __ENV.SEARCH_URL || buildDefaultSearchUrl(baseUrl);
    const thinkSec = Number(__ENV.THINK_TIME_SEC || 1);

    const homeRes = http.get(baseUrl, reqParams("homepage"));
    check(homeRes, {
        "homepage status is 200": (r) => r.status === 200,
        "homepage has site shell": (r) => r.body.includes("enuygun") && r.body.includes("ucak-bileti"),
    });

    sleep(thinkSec);

    const searchRes = http.get(searchUrl, reqParams("flight_search"));
    check(searchRes, {
        "flight_search status is 200": (r) => r.status === 200,
        "flight_search html is search shell": (r) =>
            r.body.includes("FLIGHT_FORM_CONFIG") ||
            r.body.includes("flightLegs") ||
            r.body.includes('id="SearchRoot"'),
    });

    sleep(2);
}

function timestampSuffix() {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}_${pad(d.getHours())}${pad(d.getMinutes())}`;
}

export function handleSummary(data) {
    const ts = timestampSuffix();
    return {
        "stdout": JSON.stringify(data.metrics),
        [`load-test/k6/reports/summary_${ts}.json`]: JSON.stringify(data, null, 2),
        [`load-test/k6/reports/report_${ts}.html`]: htmlReport(data),
    };
}