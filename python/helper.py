from urllib.parse import urlparse, parse_qs
import tldextract # type: ignore
import csv
import torch
import pandas as pd
import re
from sklearn.preprocessing import StandardScaler
import joblib
from nnmodel import URLBinaryClassifier, URLBinaryClassifier2  # type: ignore

def validate_url(func):
    def wrapper_validate_url(*args, **kwargs):
        self = args[0]
        url = self.url    
        url_regex = r"https?://[^\s/$.?#].[^\s]*"
        result = re.match(url_regex, url)

        if (result == None):
            raise ValueError("URL is not set or valid.")
        else: 
            return func(*args, **kwargs)
        
    return wrapper_validate_url

def get_url_components(url):
    parsed = urlparse(url)
    extracted = tldextract.extract(url)

    components = {
        'subdomain': extracted.subdomain,
        'domain': extracted.domain,
        'tld': extracted.suffix,
        'path': parsed.path,
        'query': parsed.query,
        'query_dict': parse_qs(parsed.query),
        'fragment': parsed.fragment,
        'netloc' : parsed.netloc,
        'scheme': parsed.scheme,
    }

    return components

def get_model_predictions(model_path, scaler_path, url):
    scaler = joblib.load(scaler_path) # getting standard scaler

    feature_dict = extract_url_features_v2(url)

    X = pd.DataFrame([feature_dict])
    X = scaler.transform(X)
    
    model = URLBinaryClassifier2(65)  # initialising the neural network model
    model_weights = torch.load(model_path, weights_only=True) # getting model weights
    model.load_state_dict(model_weights) # loading model weights in the model
    
    model.eval()
    with torch.no_grad():
        logits = model(torch.tensor(X, dtype=torch.float32))
        probs = torch.sigmoid(logits)
        return probs

def extract_url_features(url):
    url_components = get_url_components(url)

    features_to_iterate = ["subdomain", "domain", "tld", "path", "query", "fragment", "netloc"]
    feature_dict = {}

    feature_dict["url_length"] = len(url)
    feature_dict["url_punctuations_count"] = len(re.findall(r"[^\w\s]", url))
    feature_dict["url_digits_count"] = len(re.findall(r"\d", url))
    
    for feature in features_to_iterate:
        key = feature + "_length"
        feature_dict[key] = len(url_components[feature])
        key = feature + "_punctuations_count"
        feature_dict[key] = len(re.findall(r"[^\w\s]", url_components[feature]))
        key = feature + "_digits_count"
        feature_dict[key] = len(re.findall(r"\d", url_components[feature]))

    feature_dict["secured_scheme"] = 1 if url_components["scheme"] == "https" else 0
    return feature_dict

def extract_url_features_v2(url):
    url_components = get_url_components(url)
    netloc = url_components["netloc"]
    subdomain = url_components["subdomain"]
    tld = url_components["tld"]
    path = url_components["path"]
    query = url_components["query"]
    domain = url_components["domain"]
    fragment = url_components["fragment"]
    scheme = url_components["scheme"]
    return {
        'scheme_secured' : 1 if scheme == "https" else 0,
        'url_length' : len(url),
        #'subdomain_length' : len(subdomain),
        'domain_length' : len(domain),
        'tld_length' : len(tld),
        'path_length' : len(path),
        'query_length' : len(query),
        #'fragment_length' : len(fragment),
        'url_digits_count' : sum(c.isdigit() for c in url),
        'url_percent_count' : sum(c == '%' for c in url),
        'url_hash_count' : sum(c == '#' for c in url),
        'url_tilde_count' : sum(c == '~' for c in url),
        'url_quote_count' : sum(c == "'" for c in url),
        'url_leftsquare_count' : sum(c == '[' for c in url),
        'url_rightbracket_count' : sum(c == ']' for c in url),
        'url_dollar_count' : sum(c == '$' for c in url),
        'url_amp_count' : sum(c == '&' for c in url),
        'url_forwardslash_count' : sum(c == "\\" for c in url),
        'url_colon_count' : sum(c == ':' for c in url),
        'url_at_count' : sum(c == '@' for c in url),
        'url_plus_count' : sum(c == '+' for c in url),
        'url_coma_count' : sum(c == ',' for c in url),
        'url_semicolon_count' : sum(c == ';' for c in url),
        'url_equal_count' : sum(c == '=' for c in url),
        'url_question_count' : sum(c == '?' for c in url),
        'url_dots_count' : sum(c == '.' for c in url),
        'url_hyphen_count' : sum(c == '-' for c in url),
        'subdomain_digits_count' : sum(c.isdigit() for c in subdomain),
        'subdomain_plus_count' : sum(c == '+' for c in subdomain),
        'subdomain_dots_count' : sum(c == '.' for c in subdomain),
        'subdomain_hyphen_count' : sum(c == '-' for c in subdomain),
        'domain_digits_count' : sum(c.isdigit() for c in domain),
        'domain_dots_count' : sum(c == '.' for c in domain),
        'domain_hyphen_count' : sum(c == '-' for c in domain),
        'tld_digits_count' : sum(c.isdigit() for c in tld),
        'tld_dots_count' : sum(c == '.' for c in tld),
        'tld_hyphen_count' : sum(c == '-' for c in tld),
        'path_digits_count' : sum(c.isdigit() for c in path),
        'path_percent_count' : sum(c == '%' for c in path),
        'path_tilde_count' : sum(c == '~' for c in path),
        'path_quote_count' : sum(c == "'" for c in path),
        'path_dollar_count' : sum(c == '$' for c in path),
        'path_amp_count' : sum(c == '&' for c in path),
        'path_colon_count' : sum(c == ":" for c in path),
        'path_at_count' : sum(c == '@' for c in path),
        'path_plus_count' : sum(c == '+' for c in path),
        'path_coma_count' : sum(c == ',' for c in path),
        'path_semicolon_count' : sum(c == ';' for c in path),
        'path_equal_count' : sum(c == '=' for c in path),
        'path_dots_count' : sum(c == '.' for c in path),
        'path_hyphen_count' : sum(c == '-' for c in path),
        'query_digits_count' : sum(c.isdigit() for c in query),
        'query_percent_count' : sum(c == '%' for c in query),
        'query_tilde_count' : sum(c == '~' for c in query),
        'query_quote_count' : sum(c == "'" for c in query),
        'query_dollar_count' : sum(c == '$' for c in query),
        'query_amp_count' : sum(c == '&' for c in query),
        'query_forwardslash_count' : sum(c == "\\" for c in query),
        'query_colon_count' : sum(c == ':' for c in query),
        'query_at_count' : sum(c == '@' for c in query),
        'query_plus_count' : sum(c == '+' for c in query),
        'query_coma_count' : sum(c == ',' for c in query),
        'query_semicolon_count' : sum(c == ';' for c in query),
        'query_equal_count' : sum(c == '=' for c in query),
        'query_question_count' : sum(c == '?' for c in query),
        'query_dots_count' : sum(c == '.' for c in query),
        'query_hyphen_count' : sum(c == '-' for c in query),
    }