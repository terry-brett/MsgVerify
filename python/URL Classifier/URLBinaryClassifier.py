import torch.nn as nn
import torch.nn.functional as F
from urllib.parse import urlparse, parse_qs
import tldextract
import torch
import pandas as pd
import re
import joblib
import json

class URLBinaryClassifier(nn.Module):
    def __init__(self, input_dim):
        super(URLBinaryClassifier, self).__init__()
        self.fc1 = nn.Linear(input_dim, 128)
        self.fc2 = nn.Linear(128, 64)
        self.fc3 = nn.Linear(64, 32)
        self.output = nn.Linear(32, 1)  # Single output neuron for binary classification

    def forward(self, x):
        x = F.relu(self.fc1(x))
        x = F.relu(self.fc2(x))
        x = F.relu(self.fc3(x))
        return self.output(x)  # No sigmoid here; we'll use BCEWithLogitsLoss
    
class Helper():
    def __init__(self):
        pass
    
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

    def get_url_components(self, url):
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

    def extract_url_features(self, url):
        url_components = self.get_url_components(url)

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
    
    def predict(self, url):
        with open('config.json') as f:
            config = json.load(f)
            
        model_path = config["url_classifier_model_path"]
        scaler_path = config["url_features_transformation_scaler_path"]
        scaler = joblib.load(scaler_path) # getting standard scaler

        feature_dict = self.extract_url_features(url)

        X = pd.DataFrame([feature_dict])
        X = scaler.transform(X)
        
        model = URLBinaryClassifier(25)  # initialising the neural network model
        model_weights = torch.load(model_path, weights_only=True) # getting model weights
        model.load_state_dict(model_weights) # loading model weights in the model
        
        model.eval()
        with torch.no_grad():
            logits = model(torch.tensor(X, dtype=torch.float32))
            probs = torch.sigmoid(logits)
            return probs