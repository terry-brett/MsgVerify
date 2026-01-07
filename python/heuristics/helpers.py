import re

from sympy import true, false


# helper function to convert to lowercase and remove special characters
def normalise(message):
    text = message.lower()
    text = re.sub(r"\s+", " ", text).strip()
    return text


# marketing helpers
def has_marketing_patters(message):
    """
            Heuristic marketing detector using weighted signals.
            Returns label string if detected, else "".
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
        "sale", "deal", "discount", "offer", "promo", "promotion", "coupon",
        "save", "clearance", "limited time", "exclusive", "special offer",
        "buy one get one", "bogo", "free trial", "trial", "bonus", "cashback",
        "gift", "giveaway", "winner", "congratulations",
        "free", "free entry", "entry", "win", "prize", "competition", "comp",
        "weekly", "wkly", "tickets", "tkts", "mins", "minutes", "txt", "txts", "text"
        # legal
        "t&c", "t&cs", "terms", "apply",
        "standard txt rate", "std txt rate",
        "over18", "18+", "text rate"
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
