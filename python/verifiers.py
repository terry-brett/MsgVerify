import helper
from helper import validate_url
import json
import re
from response import URLVerifierResponse, Reason

class URLVerifier:
    
    def __init__(self, url):
        self.url = str(url)
        self.components = helper.get_url_components(url)

    @validate_url
    def is_scheme_secured(self):
        return True if (self.components["scheme"] == "https") else False
        
    @validate_url
    def is_url_ip_based(self): 
        pattern = r'(?i)\b(?:http|https|ftp)://(?:\d{1,3}\.){3}\d{1,3}(?::\d+)?(?:/[^\s]*)?'

        result = re.match(pattern,self.url)
        return False if (result == None) else True
    
    @validate_url
    def is_url_triggering_dowload(self):
        path = self.components["path"]
        download_extensions = {
            '.zip', '.rar', '.7z', '.exe', '.msi', '.pdf', '.doc', '.docx',
            '.xls', '.xlsx', '.ppt', '.pptx', '.mp3', '.mp4', '.avi', '.mkv',
            '.jpg', '.png', '.tar', '.gz', '.csv', '.json'
        }
        for ext in download_extensions:
            if path.endswith(ext):
                return True
            
        if re.search(r'/download\b|/downloads\b', path):
            return True

        query = self.components["query_dict"]
        for key, values in query.items():
            if key.lower() in {'download', 'dl', 'file'}:
                return True
            for val in values:
                if any(x in val.lower() for x in ['download', 'file', 'export']):
                    return True
                
        return False
    
    @validate_url
    def is_short_url(self):
        try:
            shortner_domains = {
                "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "buff.ly", "rebrand.ly",
                "is.gd", "soo.gd", "trib.al", "tiny.cc", "shorturl.at", "lnkd.in", "shorte.st",
                "cutt.ly", "rb.gy", "clck.ru", "chilp.it", "x.co", "qr.ae"
            }

            hostname = self.components["netloc"]

            # Remove "www." prefix for standardization
            if hostname.startswith("www."):
                hostname = hostname[4:]

            return hostname in shortner_domains
        except Exception:
            return False
    
    @validate_url
    def count_of_non_ascii_characters_in_url(self):
        return sum(1 for char in self.url if ord(char) > 127)
    
    
    @validate_url
    def get_ml_prediction(self):
        with open('config.json') as f:
            config = json.load(f)
        
        model_path = config["url_classifier_model_path"]
        scaler_path = config["url_features_transformation_scaler_path"]

        return helper.get_model_predictions(model_path, scaler_path, self.url)