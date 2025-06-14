from urllib.parse import urlparse, parse_qs
import tldextract # type: ignore

def get_url_components(url):
    parsed = urlparse(url)
    extracted = tldextract.extract(url)

    components = {
        'scheme': parsed.scheme,
        'subdomain': extracted.subdomain,
        'domain': extracted.domain,
        'tld': extracted.suffix,
        'path': parsed.path,
        'query': parsed.query,
        'query_dict': parse_qs(parsed.query),
        'fragment': parsed.fragment,
    }

    return components