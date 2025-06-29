from urllib.parse import urlparse, parse_qs
import tldextract # type: ignore
import csv
import torch
import pandas as pd
import re
from sklearn.preprocessing import StandardScaler
import joblib
from nnmodel import URLBinaryClassifier  # type: ignore

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
    model = URLBinaryClassifier(25)
    model_weights = torch.load(model_path, weights_only=True)
    model.load_state_dict(model_weights)
    scaler = joblib.load(scaler_path)

    feature_dict = extract_url_features(url)

    feature_df = pd.DataFrame([feature_dict])
    feature_df = scaler.transform(feature_df)
    X = feature_df

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