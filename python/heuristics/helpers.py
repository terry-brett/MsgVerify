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
