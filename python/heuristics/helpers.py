import re
import string
from sympy import true, false

URL_REGEX = re.compile(
    r'^(https?:\/\/)(www\.)?[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)+(:\d+)?'
    r'(\/[A-Za-z0-9._~!$&\'()*+,;=:@%-]*)*'
    r'(\?[A-Za-z0-9._~!$&\'()*+,;=:@%-]*)?'
    r'(#[A-Za-z0-9._~!$&\'()*+,;=:@%-]*)?$',
    re.IGNORECASE
)
# helper function to convert to lowercase and remove special characters
def normalise(message):
    text = message.lower()
    text = re.sub(r"\s+", " ", text).strip()
    return text


# marketing helpers
def has_marketing_patters(message):
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

def contains_url (message):
    for token in message.split():
        if URL_REGEX.match(token):
            return True
    return False
