import helper
import json
import re
from response import URLVerifierResponse, Reason

class URLVerifier:
    
    def __init__(self, url):
        self.url = str(url)
        self.components = helper.get_url_components(url)

    def is_scheme_secured(self):
        return True if (self.components["scheme"] == "https") else False
    
    def is_domain_blocklisted(self):
        with open('config.json') as f:
            config = json.load(f)
        
        filepath = config["domain_blocklist_file_path"]

        return helper.word_exists_in_csv(self.components["domain"], filepath) 
        
    def is_url_ip_based(self): 
        pattern = r'(?i)\b(?:http|https|ftp)://(?:\d{1,3}\.){3}\d{1,3}(?::\d+)?(?:/[^\s]*)?'

        result = re.match(pattern,self.url)
        return False if (result == None) else True
    
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

    def is_url_heavily_encoded(self):
        threshold = 10
        return True if self.url.count("%") > threshold else False
    
    def count_of_non_ascii_characters_in_url(self):
        return sum(1 for char in self.url if ord(char) > 127)

    def count_of_unsafe_characters_in_url(self):
        unsafe_characters = ["<", ">", "#", "{", "}", "\'", "|", "\\", "^", "~", "[", "]", "‘"]
        count = 0
        for char in unsafe_characters:
            count += self.url.count(char)

        return count
    
    def print_evaluation_report(self):
        print("Scheme Secured: " + str(self.is_scheme_secured()))
        print("Domain Blocklisted: " + str(self.is_domain_blocklisted()))
        print("IP Based URL: " + str(self.is_url_ip_based()))
        print("Triggering Download: " + str(self.is_url_triggering_dowload()))
        print("Short URL: " + str(self.is_short_url()))
        print("Heavily Encoded: " + str(self.is_url_heavily_encoded()))
        print("Non-ASCII Characters Count: " + str(self.count_of_non_ascii_characters_in_url()))
        print("Unsafe Characters Count: " + str(self.count_of_unsafe_characters_in_url()))
        
        return
    
    
        
class TextVerifier:

    def __init__(self, text):
        self.text = str(text)

    

    
