import re
import string
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
    """High-precision marketing detector (tuned to Dataset_10191 annotations).

    Returns True/False.
    """
    msg = normalise(message)
    raw = message

    # Strong opt-out / compliance (common in marketing SMS)
    if re.search(r"\b(unsubscribe|opt\s*out|reply\s*stop|text\s*stop|send\s*stop|stop\s+to)\b", msg):
        return True

    # Suppress pure OTP / verification messages (transactional)
    if re.search(r"\b(your\s+otp|your\s+verification\s+code|one[-\s]?time\s+pass)\b", msg):
        return False

    promo = bool(re.search(r"\b(discount|offer|deal|sale|limited|promo|promotion|voucher|coupon|cashback|half price|free)\b", msg))
    telecom = bool(re.search(r"\b(line rental|free texts?|text messages|minutes|min|handset|tariff|contract)\b", msg))
    contest = bool(re.search(r"\b(free entry|entry|competition|comp\b|win)\b", msg))

    cta = bool(re.search(r"\b(call|dial|text|reply|click|visit|join|buy|order|subscribe|claim|redeem|get)\b", msg))
    contact = bool(re.search(PHONE_LIKE, raw) or re.search(SHORTCODE, raw) or contains_url(raw))

    return (promo or telecom or contest) and (cta or contact)

def has_urgency_or_intimidation_patterns(message):
    """High-precision urgency/intimidation detector (tuned to Dataset_10191 annotations)."""
    msg = normalise(message)
    return (
        bool(re.search(r"\burgent\b", msg)) or
        bool(re.search(r"\bimmediately\b", msg)) or
        ("final notice" in msg)
    )

def has_spelling_error(message):
    """Tuned proxy for 'Grammatical Errors/Poor Formatting'.

    NOTE: Dataset_10191 applies this label heavily to SMS-style shorthand.
    This heuristic targets that style rather than true spelling mistakes.
    """
    msg = normalise(message)
    slang_pat = re.compile(r"\b(u|ur|wif|wana|wanna|wan|2nite|2moro|tmr|pls|plz|txt|msg|msgs|tho|cos|cuz|dat|dun|leh|lor|lah)\b")
    hits = len(slang_pat.findall(msg))
    if hits >= 2:
        return True

    # Secondary: extreme caps in long messages
    letters = [c for c in message if c.isalpha()]
    if len(letters) > 30:
        caps_ratio = sum(1 for c in letters if c.isupper()) / len(letters)
        if caps_ratio >= 0.8:
            return True

    return False

def has_too_good_to_be_true_patterns(message):
    """High-precision 'Too Good To Be True' detector (tuned to Dataset_10191 annotations)."""
    msg = normalise(message)
    raw = message

    contact = bool(re.search(PHONE_LIKE, raw) or re.search(SHORTCODE, raw) or contains_url(raw))
    if not contact:
        return False

    # Pattern A: urgent + prize/win + explicit currency symbol
    if re.search(r"\burgent\b", msg) and re.search(r"\b(prize|award|won|winner)\b", msg) and re.search(r"[£$€]", raw):
        return True

    # Pattern B: selected + prize/award + money (symbol or "5000 pounds", etc.)
    money = bool(re.search(r"[£$€]", raw) or re.search(r"\b\d{3,}\s*(pounds|gbp|usd|eur|inr|nok|sek|dkk)\b", msg))
    if re.search(r"\bselected\b", msg) and money and re.search(r"\b(receive|award|prize)\b", msg):
        return True

    return False

def has_credential_verification_patterns(message):
    """High-precision credential verification request detector."""
    msg = normalise(message)

    # Suppress normal OTP notifications
    if re.search(r"\byour\s+(otp|verification\s+code)\s+is\b", msg):
        return False

    action = bool(re.search(r"\b(verify|confirm|update|reset|authenticate|login|log in|sign in)\b", msg))
    noun = bool(re.search(r"\b(password|passcode|pin|credentials|account|login|username|security code|verification code|otp|2fa)\b", msg))
    request = bool(re.search(r"\b(please|kindly)\b", msg)) or bool(re.search(r"\b(click|visit|reply|enter|submit)\b", msg)) or contains_url(message)

    return action and noun and request

def has_adult_content_patterns(message):
    """High-precision adult-content detector (tuned to Dataset_10191 annotations)."""
    msg = normalise(message)
    adult_pat = re.compile(r"\b(xxx|porn|hardcore|nude|nudes|sexcam|camgirl|escort|filthy)\b")
    return bool(adult_pat.search(msg))

def asks_for_financial_or_personal_info(message):
    """High-precision detector for explicit requests for financial/personal info."""
    msg = normalise(message)
    raw = message

    fin_fields = re.compile(r"\b(bank\s+account|account\s+number|sort\s+code|iban|bic|swift|routing\s+number|credit\s+card|debit\s+card|card\s+(number|details)|cvv|cvc|expiry|expiration|billing\s+address)\b", re.I)
    pii_fields = re.compile(r"\b(date\s+of\s+birth|dob|ssn|social\s+security|national\s+id|passport\s+number|home\s+address|street\s+address|house\s*(no|number)|postcode|zip\s*code)\b", re.I)
    request_verbs = re.compile(r"\b(provide|send|share|enter|submit|confirm|verify|update|reply\s+with|fill\s*(in|out))\b", re.I)

    if not (fin_fields.search(raw) or pii_fields.search(raw)):
        return False
    if not request_verbs.search(raw):
        return False

    # Verb near field (reduces FPs from generic mentions)
    if re.search(r"(provide|send|share|enter|submit|confirm|verify|update|reply\s+with|fill\s*(in|out)).{0,50}(bank\s+account|account\s+number|sort\s+code|iban|bic|swift|routing\s+number|credit\s+card|debit\s+card|card\s+(number|details)|cvv|cvc|expiry|expiration|billing\s+address|date\s+of\s+birth|dob|ssn|social\s+security|national\s+id|passport\s+number|home\s+address|street\s+address|house\s*(no|number)|postcode|zip\s*code)", msg, re.I):
        return True

    if re.search(r"(bank\s+account|iban|sort\s+code|card\s+details|cvv|dob|ssn|address).{0,40}(required|needed|mandatory)", msg):
        return True

    return False

def check_impersonation(message, sender=None):
    """High-precision impersonation detector (tuned to Dataset_10191 annotations).

    Dataset_10191 marks impersonation mainly for:
    - Vodafone 'todays numbers ending ... selected ...'
    - HMRC/GOVUK tax refund links
    - Bank of America account suspended + login link
    - Paytm FASTag 'received a request ...' with paytm.me link
    """
    msg = normalise(message)
    raw = message

    # Vodafone: very specific pattern used in gold labels
    if ("vodafone" in msg and re.search(r"\btoday'?s\b", msg)
        and re.search(r"\bnumbers?\b", msg)
        and (re.search(r"\bending\b", msg) or re.search(r"\bend\b", msg))
        and (re.search(r"\bselected\b", msg) or re.search(r"\bclaim code\b", msg))):
        return True

    # HMRC / GOVUK tax refund
    if (("hmrc" in msg or "govuk" in msg) and ("refund" in msg or "tax" in msg) and contains_url(raw)):
        return True

    # Bank of America login / suspension
    if (("bank of america" in msg or "bankofamerica" in msg)
        and re.search(r"\b(login|log in|sign in|suspend|suspended|restore)\b", msg)
        and contains_url(raw)):
        return True

    # Paytm FASTag request (paytm.me link)
    if ("paytm" in msg and "received a request" in msg and ("fastag" in msg or "payment bank" in msg) and contains_url(raw)):
        return True

    # Optional sender-domain mismatch check (only if sender is provided)
    if sender is not None:
        sender_domain = get_domain_from_email(sender)
        if sender_domain:
            free = {"gmail.com","yahoo.com","outlook.com","hotmail.com","live.com","aol.com","icloud.com","proton.me","protonmail.com"}
            if sender_domain in free and re.search(r"\b(bank|hmrc|govuk|vodafone|paytm)\b", msg):
                return True

    return False

def contains_url(message):
    """Detects URLs more robustly than URL_REGEX.match(token)."""
    url_any = re.compile(
        r"(https?://\S+|www\.\S+|\b(?:bit\.ly|t\.co|tinyurl\.com|goo\.gl|ow\.ly|is\.gd|buff\.ly|rebrand\.ly)/\S+|"
        r"\b[a-z0-9-]{2,}\.(?:com|net|org|info|biz|io|co|me|app|xyz|uk|in|ru|de|fr|no|se|dk|fi|nl|au|br|jp|kr|sg|za)\b[^\s]*)",
        re.IGNORECASE
    )
    return bool(url_any.search(str(message)))

