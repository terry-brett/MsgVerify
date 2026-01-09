import re
import string
from sympy import true, false
import json

URL_REGEX = re.compile(
    r'^(https?:\/\/)(www\.)?[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)+(:\d+)?'
    r'(\/[A-Za-z0-9._~!$&\'()*+,;=:@%-]*)*'
    r'(\?[A-Za-z0-9._~!$&\'()*+,;=:@%-]*)?'
    r'(#[A-Za-z0-9._~!$&\'()*+,;=:@%-]*)?$',
    re.IGNORECASE
)

PHONE_LIKE  = r"\b(?:\+?\d[\d\s\-()]{7,}\d)\b"
SHORTCODE   = r"\b\d{5,6}\b"

BRANDS = {'phishing_targets': [{'name': 'DHL', 'abbr': 'DHL'}, {'name': 'Allied Bank Limited', 'abbr': 'ABL'}, {'name': 'Santander UK', 'abbr': 'SAN'}, {'name': 'Coinbase', 'abbr': 'COIN'}, {'name': 'East Japan Railway Company', 'abbr': 'JR East'}, {'name': 'Steam', 'abbr': 'STM'}, {'name': 'Bank Millennium', 'abbr': 'MIL'}, {'name': 'Virustotal', 'abbr': 'VT'}, {'name': 'DocuSign', 'abbr': 'DOCU'}, {'name': 'Apple', 'abbr': 'AAPL'}, {'name': 'Nationwide', 'abbr': 'NBS'} , {'name': 'Banco Bilbao Vizcaya Argentaria', 'abbr': 'BBVA'}, {'name': 'WeTransfer', 'abbr': 'WT'}, {'name': 'Adobe', 'abbr': 'ADBE'}, {'name': 'Das kann Bank', 'abbr': 'DKB'}, {'name': 'Orange', 'abbr': 'ORA'}, {'name': 'Regions Bank', 'abbr': 'RF'}, {'name': 'Allegro', 'abbr': 'ALE'}, {'name': 'Royal Bank of Canada', 'abbr': 'RY'}, {'name': 'AEON Card', 'abbr': 'AEON'}, {'name': 'Microsoft', 'abbr': 'MSFT'}, {'name': 'The Brazilian Development Bank', 'abbr': 'BNDES'}, {'name': 'Caixa', 'abbr': 'CEF'}, {'name': 'Dropbox', 'abbr': 'DBX'}, {'name': 'Comcast', 'abbr': 'CMCSA'}, {'name': 'Wachovia', 'abbr': 'WB'}, {'name': 'Mercari', 'abbr': 'MERC'}, {'name': 'Other', 'abbr': 'MISC'}, {'name': 'HSBC Group', 'abbr': 'HSBC'}, {'name': 'Wells Fargo', 'abbr': 'WFC'}, {'name': "Her Majesty's Revenue and Customs", 'abbr': 'HMRC'}, {'name': 'US Bank', 'abbr': 'USB'}, {'name': 'PayPay Bank', 'abbr': 'PPB'}, {'name': 'Aetna Health Plans & Dental Coverage', 'abbr': 'AET'}, {'name': 'Telefónica UK', 'abbr': 'O2'}, {'name': 'Visa', 'abbr': 'V'}, {'name': 'Banco De Brasil', 'abbr': 'BBAS'}, {'name': 'UniCredit', 'abbr': 'UCG'}, {'name': 'PKO Polish Bank', 'abbr': 'PKO'}, {'name': 'Bradesco', 'abbr': 'BBD'}, {'name': 'AT&T', 'abbr': 'T'}, {'name': 'Barclays Bank PLC', 'abbr': 'BARC'}, {'name': 'Co-operative Bank', 'abbr': 'COOP'}, {'name': 'Huntington National Bank', 'abbr': 'HBAN'}, {'name': 'ABN AMRO Bank', 'abbr': 'ABN'}, {'name': 'Internal Revenue Service', 'abbr': 'IRS'}, {'name': 'RuneScape', 'abbr': 'RS'}, {'name': 'Sumitomo Mitsui Banking Corporation', 'abbr': 'SMBC'}, {'name': 'Banco Santander, S.A.', 'abbr': 'SAN'}, {'name': 'Navy Federal Credit Union', 'abbr': 'NFCU'}, {'name': 'Netflix', 'abbr': 'NFLX'}, {'name': 'JPMorgan Chase and Co.', 'abbr': 'JPM'}, {'name': 'Bank of America Corporation', 'abbr': 'BAC'}, {'name': 'Raiffeisen Bank', 'abbr': 'RBI'}, {'name': 'Yahoo', 'abbr': 'YHOO'}, {'name': 'Accurint', 'abbr': 'ACC'}, {'name': 'Rakuten', 'abbr': 'RKUNY'}, {'name': 'British Telecom', 'abbr': 'BT'}, {'name': 'Hotmail', 'abbr': 'MSFT'}, {'name': 'AOL', 'abbr': 'AOL'}, {'name': 'Google', 'abbr': 'GOOGL'}, {'name': 'Intesa Sanpaolo', 'abbr': 'ISP'}, {'name': 'Volksbanken Raiffeisenbanken', 'abbr': 'VR'}, {'name': 'TSB', 'abbr': 'TSB'}, {'name': 'Nets', 'abbr': 'NETS'}, {'name': 'Binance', 'abbr': 'BNB'}, {'name': 'Itau', 'abbr': 'ITUB'}, {'name': 'Mastercard', 'abbr': 'MA'}, {'name': 'American Express', 'abbr': 'AMEX'}, {'name': 'ABSA Bank', 'abbr': 'ABSA'}, {'name': 'Westpac', 'abbr': 'WBC'}, {'name': 'PayPal', 'abbr': 'PYPL'}, {'name': 'Facebook', 'abbr': 'META'}, {'name': 'Swiss Post', 'abbr': 'SWP'}, {'name': 'Abonné Free Mobile', 'abbr': 'FREE'}, {'name': 'Nubank', 'abbr': 'NU'}, {'name': 'eBay, Inc.', 'abbr': 'EBAY'}, {'name': 'Nordea Bank', 'abbr': 'NDA'}, {'name': 'Development Bank of Singapore', 'abbr': 'DBS'}, {'name': 'Rackspace', 'abbr': 'RAX'}, {'name': 'Scotiabank', 'abbr': 'BNS'}, {'name': 'Interactive Brokers', 'abbr': 'IBKR'}, {'name': 'ING Direct', 'abbr': 'ING'}, {'name': 'Amazon.com', 'abbr': 'AMZN'}, {'name': 'Optus', 'abbr': 'OPT'}, {'name': 'Sulake Corporation', 'abbr': 'SUL'}, {'name': 'Instagram', 'abbr': 'IG'}, {'name': 'Capital One', 'abbr': 'COF'}, {'name': 'Capitec Bank', 'abbr': 'CPI'}]}

# helper function to convert to lowercase and remove special characters
def normalise(message):
    text = message.lower()
    text = re.sub(r"\s+", " ", text).strip()
    return text

def extract_email_address(sender: str):
    if not sender:
        return None
    m = re.search(r"([A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,})", sender)
    return m.group(1).lower() if m else None

def get_domain_from_email(email: str):
    if not email or "@" not in email:
        return None
    return email.split("@", 1)[1].lower()

# marketing helpers
def has_marketing_patterns(message):
    """
        Heuristic marketing detector using weighted signals.
        Returns true/false
    """
    score = 0
    reasons = []
    # opt - out / compliance phrases(very common in marketing SMS / email)
    opt_out_patterns = [
        r"\bunsubscribe\b",
        r"\bopt\s*out\b",
        r"\bmanage\s+preferences\b",
        r"\bstop\s+to\s+(end|cancel|unsubscribe)\b",
        r"\breply\s+stop\b",
        r"\btext\s+stop\b",
        r"\bcall2optout\b",
        r"\boptout\b",
    ]

    if any(re.search(p, message) for p in opt_out_patterns):
        score += 4
        reasons.append(("opt_out_language", 4))

    # promotions / discounts / deals
    promo_keywords = [
        "congratulations", "enjoy", "free", "promo", "free trial", "bonus", "best price", "lowest price",
        "competition", "text", "t&c", "weekly", "t&cs", "best deals", "free", "promotion", "gift", "winner",
        "special offer", "prize", "comp", "standard txt rate", "exclusive", "giveaway", "discount",
        "std txt rate", "mins", "18+", "minutes", "sale", "over18", "call now", "deals", "apply", "offer",
        "txt", "trial", "deal", "txts", "tickets", "clearance", "coupon", "text rate", "limited time",
        "cashback", "wkly", "bogo", "tkts", "save", "entry", "offers", "best deal", "free entry", "terms",
        "available now", "sale", "book now", "buy one get one", "win"
    ]

    promo_hits = sum(1 for k in promo_keywords if k in message)
    if promo_hits:
        add = min(5, 1 + promo_hits)
        score += add
        reasons.append(("promo_hits", add))

    # call to action / shopping intents
    cta_patterns = [
        r"\bbuy\s+now\b",
        r"\bshop\s+now\b",
        r"\border\s+now\b",
        r"\bget\s+started\b",
        r"\bsign\s+up\b",
        r"\bsubscribe\b",
        r"\bjoin\s+now\b",
        r"\bdownload\b",
        r"\bclaim\b",
        r"\bredeem\b",
        r"\bcheck\s+out\b",
        r"\bcall\b\s*(?:now\s*)?\b",  # "call now"
        r"\btext\b\s+\w+\s+\bto\b\s+\d{4,6}\b",  # "Text FA to 87121"
        r"\btxt\b\s+\w+\s+\bto\b\s+\d{4,6}\b",  # "txt FA to 87121"
        r"\blucky\s*draw\b",
        r"\bcontest\b",
        r"\bwinner\b",
        r"\bsubscribe\b",
        r"\bsubscribed\b",
        r"\bsubscriber\b",
        r"\byou\s+are\s+the\s+winner\b",
        r"\bwon\b",
        r"\bprize\b",
        r"\baward\b",
        r"\bcongratulations\b",
    ]

    if any(re.search(p, message) for p in cta_patterns):
        score += 2
        reasons.append(("cta_language", 2))

    # price indicators, percentage
    price_patterns = [
        r"[$€£]\s?\d+(?:[.,]\d{2})?\b",  # $19.99, €100
        r"\b\d+(?:[.,]\d{2})?\s?(usd|eur|gbp|nok|sek|dkk|pln)\b",
        r"\bkr\s?\d+\b",  # Norwegian "kr 199"
        r"\b\d{1,3}%\s*off\b",  # 50% off
        r"\boff\s*\d{1,3}%\b",  # off 50%
    ]

    if any(re.search(p, message) for p in price_patterns):
        score += 2
        reasons.append(("price_or_discount_format", 2))

    # sales emphasis e.g. sale!!
    exclamations = message.count("!")
    if exclamations >= 3:
        score += 1
        reasons.append(("excess_exclamation", 1))

    # new product / launch
    launch_keywords = ["new", "just launched", "now available", "introducing", "release"]
    launch_hits = sum(1 for k in launch_keywords if k in message)
    if launch_hits >= 2:
        score += 1
        reasons.append(("launch_language", 1))

    transactional_patterns = [
        r"\b(receipt|invoice|order\s+confirmation|tracking|shipped|delivered)\b",
        r"\b(otp|one[-\s]?time\s+pass(code|word)|verification\s+code)\b",
    ]
    if any(re.search(p, message) for p in transactional_patterns) and score <= 4:
        # if it looks purely transactional and marketing score isn't strong, suppress.
        return ""

    if score >= 4:
        return true

    return false


def has_urgency_or_intimidation_patterns(message):
    """
        Heuristic urgency/intimidation detector using weighted signals.
        Returns true/false
    """
    raw = message
    msg = re.sub(r"\s+", " ", message.lower()).strip()

    score = 0
    reasons = []

    # direct urgency language
    urgency_keywords = [
        "urgent", "immediately", "asap", "act now", "action required", "time sensitive",
        "right away", "respond now", "do it now", "final notice", "last chance",
        "limited time", "expires", "expiring", "today", "now"
    ]
    hits = [k for k in urgency_keywords if k in msg]
    if hits:
        add = min(3, 1 + len(hits))  # cap so it doesn't explode
        score += add
        reasons.append(f"urgency_keywords(+{add})")

    # deadline/time-window patterns
    deadline_patterns = [
        r"\bwithin\s+\d+\s*(minutes?|mins?|hours?|hrs?|days?)\b",
        r"\bin\s+\d+\s*(minutes?|mins?|hours?|hrs?|days?)\b",
        r"\b\d+\s*(minutes?|mins?|hours?|hrs?)\s*(left|remaining)\b",
        r"\bby\s+(today|tonight|tomorrow)\b",
        r"\b24\s*hours?\b",
        r"\b48\s*hours?\b",
    ]
    if any(re.search(p, msg) for p in deadline_patterns):
        score += 3
        reasons.append("deadline_language(+3)")

    # account/service consequences (pressure)
    consequence_patterns = [
        r"\b(account|card|service|subscription|profile)\b.*\b(suspend|suspended|disable|disabled|block|blocked|close|closed|terminate|terminated|restricted|locked)\b",
        r"\b(will be|has been)\b.*\b(suspended|disabled|blocked|closed|locked|restricted)\b",
        r"\bavoid\b.*\b(suspension|termination|closure|penalty)\b",
    ]
    if any(re.search(p, msg) for p in consequence_patterns):
        score += 3
        reasons.append("account_consequence(+3)")

    # legal / police / intimidation threats
    intimidation_patterns = [
        r"\blegal\s+action\b",
        r"\bcourt\b",
        r"\blawsuit\b",
        r"\bpolice\b",
        r"\barrest\b",
        r"\bwarrant\b",
        r"\bprosecut(e|ion)\b",
        r"\bfine\b",
        r"\bpenalt(y|ies)\b",
        r"\bjail\b",
        r"\bscam investigation\b",
        r"\btax\b.*\b(overdue|debt|owed)\b",
        r"\bdebt collectors?\b",
    ]
    if any(re.search(p, msg) for p in intimidation_patterns):
        score += 4
        reasons.append("legal_or_threat(+4)")

    # verbs such as must, required & direct command
    command_patterns = [
        r"\byou\s+must\b",
        r"\brequired\b",
        r"\bverify\s+now\b",
        r"\bupdate\s+now\b",
        r"\bconfirm\s+now\b",
        r"\brespond\s+immediately\b",
    ]
    if any(re.search(p, msg) for p in command_patterns):
        score += 2
        reasons.append("command_language(+2)")

    # exclamation marks and CAAAPS
    if raw.count("!") >= 3:
        score += 1
        reasons.append("many_exclamations(+1)")

    letters = [c for c in raw if c.isalpha()]
    if letters:
        caps_ratio = sum(1 for c in letters if c.isupper()) / len(letters)
        if caps_ratio > 0.35:
            score += 1
            reasons.append("high_caps_ratio(+1)")

    suppression_patterns = [
        r"\b(otp|one[-\s]?time\s+pass(code|word)|verification\s+code)\b",
        r"\b(delivered|delivery|courier|tracking)\b",
    ]
    if any(re.search(p, msg) for p in suppression_patterns) and score <= 4:
        return False, reasons + ["suppressed_transactional"]

    if score >= 5:
        return true

    return false

def has_spelling_error(message) :
    contains_error = False

    with open("wordlist.txt", encoding="utf-8") as f:
        DICTIONARY = set(w.strip().lower() for w in f if w.strip())

    #ignore URLs for spell check
    text_no_urls = URL_REGEX.sub("", message)
    text_clean = re.sub(r'[\d' + re.escape(string.punctuation) + r']', '', text_no_urls)

    #split text into words
    words = text_clean.split()
    
    for i, word in enumerate(words):
        #skip capitalized words
        if word[0].isupper():
            continue
        
        #skip all caps letters
        if word.isupper():
            continue

        #check lowercase version in dictionary
        if word.lower() not in DICTIONARY:
            contains_error = True

    return contains_error

def has_too_good_to_be_true_patterns(message):
    # normalize common obfuscations: "0f" -> "of"
    msg = re.sub(r"\b0f\b", "of", message)

    score = 0
    reasons = []

    win_patterns = [
        r"\bwon\b", r"\bwinner\b", r"\bwin\b",
        r"\blottery\b", r"\blucky\s*draw\b", r"\bjackpot\b",
        r"\bprize\b", r"\bclaim\s+your\s+prize\b",
        r"\bcongratulations\b",
    ]
    if any(re.search(p, msg) for p in win_patterns):
        score += 4
        reasons.append("win_or_lottery(+4)")

    selected_patterns = [
        r"\byou(\s+have|'ve)\s+been\s+selected\b",
        r"\bselected\b",
        r"\beligible\b",
        r"\bqualif(ied|y)\b",
        r"\bpre[-\s]?approved\b",
        r"\bexclusive\s+invite\b",
    ]
    if any(re.search(p, msg) for p in selected_patterns):
        score += 2
        reasons.append("selected_or_eligible(+2)")

    free_reward_patterns = [
        r"\bfree\b",
        r"\bgift\s*card\b",
        r"\breward(s)?\b",
        r"\bbonus\b",
        r"\bcash\s*back\b",
        r"\bvoucher\b",
        r"\bcoupon\b",
    ]
    if any(re.search(p, msg) for p in free_reward_patterns):
        score += 2
        reasons.append("free_or_rewards(+2)")

    compensation_patterns = [
        r"\bcompensation\b",
        r"\bsettlement\b",
        r"\brefund\b",
        r"\breimbursement\b",
        r"\bclaim\s+refund\b",
    ]
    if any(re.search(p, msg) for p in compensation_patterns):
        score += 1
        reasons.append("refund_or_compensation(+1)")

    money_patterns = [
        r"\b(?:usd|eur|gbp|nok|sek|dkk|inr)\b",
        r"[$€£]\s?\d+(?:[.,]\d{2})?\b",
        r"\b\d{1,3}(?:[.,]\d{3})+(?:[.,]\d{2})?\b",  # 12,60000 / 1,000,000 etc (loose)
        r"\b\d+\s*(?:million|billion|crore|lakhs?|lakh)\b",
        r"\bcash\b",
    ]
    if any(re.search(p, msg) for p in money_patterns):
        score += 2
        reasons.append("money_amount_or_currency(+2)")

    claim_patterns = [
        r"\bclaim\b",
        r"\bcollect\b",
        r"\bredeem\b",
        r"\bget\s+your\b",
    ]
    if any(re.search(p, msg) for p in claim_patterns):
        score += 1
        reasons.append("claim_cta(+1)")

    if score >= 5:
        return true

    return false

def has_credential_verification_patterns(message):

    msg = re.sub(r"\b0f\b", "of", message)

    score = 0
    reasons = []

    cred_nouns = [
        "password", "passcode", "pin", "login", "log in", "signin", "sign in",
        "username", "user id", "account", "credentials", "authentication",
        "2fa", "two factor", "verification code", "security code", "otp"
    ]
    noun_hits = [w for w in cred_nouns if w in msg]
    if noun_hits:
        add = 2 if len(noun_hits) == 1 else 3
        score += add
        reasons.append(f"cred_nouns(+{add})")

    action_patterns = [
        r"\bverify\b",
        r"\bconfirm\b",
        r"\bvalidate\b",
        r"\bupdate\b",
        r"\breset\b",
        r"\bre-?login\b",
        r"\bauthenticate\b",
        r"\bsecure\b",
        r"\brestore\b",
        r"\brecover\b",
    ]
    if any(re.search(p, msg) for p in action_patterns):
        score += 3
        reasons.append("verification_or_update_action(+3)")

    strong_phrases = [
        r"\bverify\s+your\s+(account|password|login|identity)\b",
        r"\bconfirm\s+(your\s+)?(login|account)\b",
        r"\bupdate\s+your\s+(credentials|password|account)\b",
        r"\breset\s+your\s+password\b",
        r"\bsign\s*in\s+to\b.*\baccount\b",
    ]
    if any(re.search(p, msg) for p in strong_phrases):
        score += 3
        reasons.append("strong_credential_phrase(+3)")

    if re.search(URL_REGEX, message) and (
            ("password" in msg) or ("login" in msg) or ("sign in" in msg) or ("verify" in msg)):
        score += 2
        reasons.append("link_plus_credentials(+2)")

    otp_sender_patterns = [
        r"\byour\s+(otp|one[-\s]?time\s+pass(code|word)|verification\s+code)\s+is\b",
        r"\buse\s+\d{4,8}\s+as\s+your\s+(otp|code)\b",
    ]

    if any(re.search(p, msg) for p in otp_sender_patterns):
        score += 2
        reasons.append("suppressed_otp_sender_message")

    if score >= 6:
        return true

    return false

def has_adult_content_patterns(message):
    score = 0

    msg = re.sub(r"\b0f\b", "of", message)

    # strong adult slang / explicit terms (add gradually based on your dataset)
    adult_terms = [
        "rude chat", "sex", "sexy", "nude", "nudes", "naked", "porn", "xxx",
        "shag", "shagged", "cum", "hookup", "escort",
        "pics", "pictures", "gettin", "getting"
    ]
    if any(term in msg for term in adult_terms):
        score += 4

    # “private line/chat line” style + phone number is a huge signal
    if re.search(r"\b(private line|chat line|rude chat|adult chat)\b", msg):
        score += 3

    # “text PIX to 85…” / shortcodes + adult context
    if re.search(r"\btext\b.*\b(pix|pics|photo|photos)\b", msg):
        score += 2

    if re.search(PHONE_LIKE, message) or re.search(SHORTCODE, message):
        score += 1

    return score >= 6

def asks_for_financial_or_personal_info(message):

    msg = re.sub(r"\b0f\b", "of", message)

    score = 0
    reasons = []

    request_patterns = [
        r"\bprovide\b", r"\bsend\b", r"\bshare\b", r"\breply to\b", r"\breply with\b",r"\benter\b",
        r"\bsubmit\b", r"\bconfirm\b", r"\bverify\b", r"\bupdate\b", r"\bfurnish\b",
        r"\bgive\b", r"\btype\b", r"\bfill\s+(in|out)\b",
    ]
    if any(re.search(p, msg) for p in request_patterns):
        score += 2
        reasons.append("request_verb(+2)")

    financial_patterns = [
        r"\bbank\s+account\b",
        r"\baccount\s+number\b",
        r"\bsort\s+code\b",
        r"\biban\b",
        r"\bbic\b|\bswift\b",
        r"\brouting\s+number\b",
        r"\bcredit\s+card\b|\bdebit\s+card\b|\bcard\s+details\b",
        r"\bcard\s+number\b",
        r"\bcvv\b|\bcvc\b|\bsecurity\s+code\b",
        r"\bexpiry\b|\bexpiration\b|\bexp\s*date\b",
        r"\bpin\b",  # careful: overlaps with credential PIN; still sensitive
        r"\bpaypal\b.*\b(email|account)\b",
    ]

    fin_hit = any(re.search(p, msg) for p in financial_patterns)

    personal_patterns = [
        r"\bfull\s+name\b|\blegal\s+name\b|\bname\b",
        r"\bdate\s+of\s+birth\b|\bdob\b|\bbirth\s+date\b",
        r"\bssn\b|\bsocial\s+security\b|\bnational\s+id\b|\bid\s+number\b",
        r"\baddress\b|\bhome\s+address\b|\bstreet\b|\bhouse\s*(no|number)\b",
        r"\bpostcode\b|\bzip\s*code\b",
        r"\bpassport\b|\bdriver'?s\s+licen[cs]e\b",
        r"\bmother'?s\s+maiden\s+name\b",
    ]

    pers_hit = any(re.search(p, msg) for p in personal_patterns)

    if fin_hit:
        score += 3
        reasons.append("financial_field(+3)")
    if pers_hit:
        score += 3
        reasons.append("personal_field(+3)")

    if re.search(r"\b(name|address|dob|ssn|iban|account|card)\b.*[,/].*\b(name|address|dob|ssn|iban|account|card)\b",
                 msg):
        score += 2
        reasons.append("multiple_fields_listed(+2)")

    if re.search(r"(https?://\S+|www\.\S+)", message) and score >= 5:
        score += 1
        reasons.append("link_plus_request(+1)")

    informational_patterns = [
        r"\bending\s+\d{2,4}\b",
        r"\b(last\s+)?\d{4}\b",  # last 4 digits mention
        r"\baccount\s+ending\b",
        r"\bmasked\b",
        r"\bxxxx\b|\b\*{2,}\d{2,4}\b",
        r"\byour\s+(account|card|iban)\b.*\b(is|was)\b",  # "your account number is ..."
    ]

    if any(re.search(p, msg) for p in informational_patterns):
        score += 4
        reasons.append("suppressed_informational_context")

    if score >= 6:
        return True

    return False


import re


def check_impersonation(message, sender=None):
    sender = "" if sender is None else sender.lower()

    email_match = re.search(r'[\w\.-]+@[\w\.-]+\.\w+', sender)
    sender_domain = email_match.group(0).split('@')[-1] if email_match else ""

    free_providers = ['gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com', 'aol.com', 'protonmail.com']

    for brand in BRANDS['phishing_targets']:
        name = brand['name'].lower()
        abbr = brand['abbr'].lower()

        brand_pattern = rf"\b({re.escape(name)}|{re.escape(abbr)})\b"

        brand_in_msg = re.search(brand_pattern, message)
        brand_in_sender = name in sender or abbr in sender

        if brand_in_msg or brand_in_sender:
            if sender_domain:
                brand_domain_part = name.replace(" ", "").replace(".com", "")
                if sender_domain in free_providers or brand_domain_part not in sender_domain:
                    return True
            else:
                has_url = "http" in message or ".com/" in message or ".net/" in message

                if has_url:
                    return True

    return False

def contains_url (message):
    for token in message.split():
        if URL_REGEX.match(token):
            return True
    return False
