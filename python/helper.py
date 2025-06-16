from urllib.parse import urlparse, parse_qs
import tldextract # type: ignore
import csv


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

def word_exists_in_csv(word, csv_file_path, column_index=0, case_sensitive=False):
    target = word if case_sensitive else word.lower()

    with open(csv_file_path, mode='r', newline='', encoding='utf-8') as file:
        reader = csv.reader(file)
        for row in reader:
            if len(row) > column_index:
                cell_value = row[column_index]
                if not case_sensitive:
                    cell_value = cell_value.lower()
                if cell_value == target:
                    return True
    return False

def count_matching_phrases():
    return
