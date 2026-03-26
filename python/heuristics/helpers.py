import re
from spellchecker import SpellChecker
from constants import *
# Initialize spell checker once at module level for performance
_spell = SpellChecker()


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
    """Detect grammatical errors and poor formatting using composite scoring.

    Based on analysis of corrected datasets (1,849 true positives):
    - Letter spacing: 18% (V I A G R A)
    - Excessive punctuation: 16% (!!!)
    - Missing spaces: 38% (winner.Claim)
    - No sentence ending: 66%

    Uses composite scoring for real-world applicability:
    - Score >= 4: High precision, medium-high recall
    - Focuses on universal patterns, not dataset-specific text-speak
    - Suppresses professional marketing (intentional abbreviations)
    """
    if not message or len(str(message).strip()) < 10:
        return False

    msg = normalise(message)
    raw = str(message)

    # === SUPPRESSION: Professional marketing ===
    # After annotation correction, most "text-speak" was intentional marketing style
    if re.search(
        r"\b(unsubscribe|opt\s*out|reply\s*stop|text\s*stop|send\s*stop|stop\s+to|"
        r"terms\s+and\s+conditions|t&c|pobox|po\s*box|customer\s+services?|"
        r"competition|discount|offer|sale|promo|voucher|coupon|"
        r"line\s+rental|tariff|contract|cashback)\b",
        msg, re.I
    ):
        return False

    # === COMPOSITE SCORING ===
    score = 0

    # Pattern 1: Letter spacing (3 points) - High precision indicator
    # Frequency: 18% of true positives
    # Example: "V I A G R A", "C I A L I S"
    if re.search(r"\b[A-Z]\s+[A-Z]\s+[A-Z]\s+[A-Z]", raw):
        score += 3

    # Pattern 2: Excessive punctuation (2 points) - Medium-high precision
    # Frequency: 16% of true positives
    # Example: "Amazing!!!", "Why???", "....."
    if re.search(r"[!?]{3,}|[.]{4,}", raw):
        score += 2

    # Pattern 3: Missing space after punctuation (2 points) - Medium precision
    # Frequency: 38% of true positives
    # Example: "Get started.It's free", "Warning!Your account"
    if re.search(r"[.!?][A-Z](?![A-Z]{2})", raw):
        score += 2

    # Pattern 4: Multiple spaces (1 point) - Low weight, supplementary
    # Frequency: 57% of true positives
    # Example: "You have won  a  prize" (2+ consecutive spaces)
    if re.search(r"\s{2,}", raw):
        score += 1

    # Pattern 5: Character substitution obfuscation (3 points) - Email spam pattern
    # Example: "V@gra", "C!alis", "M0rtgage"
    if re.search(r"[a-zA-Z][0@!$][a-zA-Z]", msg):
        score += 3

    # Pattern 6: Informal text-speak in non-marketing context (1 point each, max 2)
    # Only count if NOT in professional marketing context
    # Frequency: 38% of true positives
    informal_patterns = [
        bool(re.search(r"\bur\b", msg, re.I)),  # "ur" for "your"
        bool(re.search(r"(?:^|\s)[Uu](?:\s|$|[.,!?])", raw)),  # "u" for "you"
        bool(re.search(r"\b(?:u|you)\s+r\b", msg, re.I)),  # "u r" for "you are"
        bool(re.search(r"\b(pls|plz|thx|thanx|rply)\b", msg, re.I)),  # Very informal abbrevs
    ]
    informal_count = sum(informal_patterns)
    if informal_count >= 2:
        score += min(informal_count, 2)  # Cap at 2 points

    # Pattern 7: Spelling errors (use spellchecker for verification)
    words = re.findall(r'\b[a-zA-Z]{4,}\b', raw)
    if words and len(words) >= 5:
        misspelled = _spell.unknown(words)
        # Filter out common false positives
        acceptable = {
            'txt', 'msg', 'mins', 'mths', 'wks', 'hrs', 'gb', 'mb', 'ltd', 'plc', 'www', 'http', 'https',
            'vodafone', 'orange', 'tmobile', 'nokia', 'samsung', 'iphone', 'pobox',
            'paypal', 'ebay', 'amazon', 'netflix', 'google', 'facebook', 'instagram', 'whatsapp',
            'okay', 'asap', 'faq', 'sms', 'mms', 'apr', 'aug', 'sep', 'oct', 'nov', 'dec'
        }
        actual_errors = [w for w in misspelled if w.lower() not in acceptable and len(w) > 3]
        error_rate = len(actual_errors) / len(words)

        # High error rate (2 points for severe, 1 point for moderate)
        if error_rate > 0.30 and len(actual_errors) >= 4:
            score += 2
        elif error_rate > 0.20 and len(actual_errors) >= 3:
            score += 1

    # === DECISION: Threshold >= 5 for high precision ===
    # Adjusted to >= 5 for better precision on small sample sizes
    return score >= 5

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
    """Detect requests for financial or personal information using composite scoring.

    Based on analysis across SMS, Email, and Twitter datasets:
    - Focus on IDENTITY info: name, address, phone, DOB, SSN, account numbers, credit cards
    - EXCLUDE login credentials (username/password/PIN/OTP) - those go to Credential Verification
    - Multi-field personal info requests (name + address + phone/age)
    - Prize scam requests for details
    - "I need your [info] asap" patterns
    - Email advance-fee scams requesting contact/cooperation/assistance

    Balanced approach:
    - Explicit requests (SMS/Twitter style): high scoring
    - Implicit requests (Email advance-fee scams): moderate scoring
    - Lower threshold to catch both types
    """
    if not message:
        return False

    msg = normalise(message)
    raw = str(message)

    # === EXCLUSIONS: Not personal info requests ===

    # Exclude: Credential verification (login/password/PIN requests)
    # These belong to "Credential Verification Request", not "Financial or Personal Information"
    credential_request = bool(re.search(
        r"\b(login|log\s*in|sign\s*in|username|user\s*id|password|passcode|pin\s*code|pin\b|"
        r"otp|verification\s+code|security\s+code|authenticate|apple\s+id)\b.{0,60}"
        r"\b(verify|confirm|send|provide|enter|update)\b",
        msg, re.I
    )) or bool(re.search(
        r"\b(verify|confirm|send|provide|enter|update).{0,60}"
        r"\b(login|log\s*in|sign\s*in|username|user\s*id|password|passcode|pin\s*code|pin\b|"
        r"otp|verification\s+code|security\s+code|authenticate|apple\s+id)\b",
        msg, re.I
    ))

    if credential_request:
        return False

    # Exclude: Vague "update security details" without specific info types
    # Example: "Please update security details at [link]" - this is link click pressure
    vague_security = bool(re.search(
        r"\bupdate\s+(your\s+)?security\s+details\b",
        msg, re.I
    )) and not bool(re.search(
        r"\b(name|address|phone|mobile|postcode|account\s+number|credit\s+card|ssn)\b",
        msg, re.I
    ))

    if vague_security:
        return False

    # Exclude: Telecom promotional offers for credit/rewards (legitimate marketing)
    # Example: "REMINDER FROM O2: To get 2.50 pounds free call credit... reply with your name..."
    # These are legitimate marketing offers from real carriers, not phishing
    telecom_promo = bool(re.search(
        r"\b(o2|orange|vodafone|t-?mobile|three|ee|verizon|at&t|sprint)\b",
        msg, re.I
    )) and bool(re.search(
        r"\b(free\s+call\s+credit|free\s+credit|reward|points|cashback|loyalty)\b",
        msg, re.I
    ))

    if telecom_promo:
        return False

    # Only suppress dating/flirting services with EXAMPLE formatting
    # Example: "REPLY with NAME & AGE eg Sam 25" - this is just for dating/flirting
    casual_dating_with_example = bool(re.search(
        r"\b(meet\s+someone|find\s+a\s+date|flirt).{0,60}\b(eg|e\.g\.|example)\s+\w+\s+\d+",
        msg, re.I
    ))

    if casual_dating_with_example:
        return False

    # === COMPOSITE SCORING ===
    score = 0

    # Pattern 1: Sensitive financial/identity info requested (6 points) - VERY high precision
    # Example: "Verify your account number", "Enter credit card details", "Send your SSN"
    # NOTE: Excludes login credentials (password/PIN) - those are handled above
    sensitive_fin = re.compile(
        r"\b(account\s+number|credit\s+card|debit\s+card|cvv|cvc|ssn|social\s+security|"
        r"sort\s+code|routing\s+number|iban|bank\s+details|credit\s+card\s+number)\b",
        re.I
    )
    if sensitive_fin.search(raw):
        # Must have request context
        if re.search(r"\b(provide|send|enter|verify|confirm|update|share|give|submit)\b", msg, re.I):
            score += 6

    # Pattern 2: Multiple personal info fields requested (6 points) - High precision
    # Example: "Send name, phone number, address to claim"
    # Example: "Reply with your name and address"
    has_name = bool(re.search(r"\b(your\s+)?name\b", msg, re.I))
    has_address = bool(re.search(r"\b(your\s+)?(address|house\s+no)\b", msg, re.I))
    has_phone = bool(re.search(r"\b(your\s+)?(phone|mobile|cell|number)\b", msg, re.I))
    has_age_dob = bool(re.search(r"\b(your\s+)?(age|dob|date\s+of\s+birth|d\.o\.b\.?)\b", msg, re.I))
    has_postcode = bool(re.search(r"\b(your\s+)?(postcode|zip\s*code|postal\s*code)\b", msg, re.I))

    field_count = sum([has_name, has_address, has_phone, has_age_dob, has_postcode])

    # Detect request verbs
    has_request_verb = bool(re.search(
        r"\b(send|reply|provide|text|forward|email|give|share)\b",
        msg, re.I
    ))

    # 3+ fields with request verb = clear info collection
    if field_count >= 3 and has_request_verb:
        score += 6
    # 2 fields (name+address OR address+age) with request verb
    elif field_count == 2 and has_request_verb:
        # Check if it's name+address or address+dob (common in scams)
        if (has_name and has_address) or (has_address and has_age_dob):
            score += 5

    # Pattern 3: Prize/lottery scam + personal info request (6 points) - Very high precision
    # Common scam pattern: "You won $1000! Send name/address/phone to claim"
    # Example: "Won 2Crores... Send Name Phone Number Address Age To Email"
    prize_pattern = bool(re.search(
        r"\b(won|winner|winning|prize|award|claim|collect|receive|redeem|gain(?:ed)?)\b",
        msg, re.I
    ))
    # Look for explicit multi-field request in prize context
    explicit_fields = bool(re.search(
        r"\b(send|provide|reply|forward).{0,80}(name.{0,40}(address|phone|number|age|postcode)|"
        r"address.{0,40}(name|phone|number|age)|phone.{0,40}(name|address))\b",
        msg, re.I
    ))
    if prize_pattern and explicit_fields:
        score += 6

    # Pattern 4: Urgent request for account/identity information (4 points)
    # Example: "URGENT: Verify your account details immediately"
    # Must be specific about WHAT information (not just "verify account")
    urgent_info_request = bool(re.search(
        r"\b(verify|confirm|update|validate|provide|send).{0,50}"
        r"\b(account\s+number|identity|personal\s+information|contact\s+details|billing\s+information)\b",
        msg, re.I
    ))
    has_urgency = bool(re.search(r"\b(urgent|immediately|now|today|asap)\b", msg, re.I))
    if urgent_info_request:
        score += 4
        if has_urgency:
            score += 1  # Bonus for urgency

    # Pattern 5: Account access restricted + specific info needed (4 points) - High precision
    # Example: "Account suspended - provide your account number to restore access"
    # Must mention SPECIFIC information type, not just vague "details"
    restricted = bool(re.search(
        r"\b(suspended|locked|restricted|limited|blocked|disabled|frozen).{0,80}(account|access|card)\b",
        msg, re.I
    ))
    specific_info_needed = bool(re.search(
        r"\b(provide|verify|confirm|update).{0,50}"
        r"\b(account\s+number|personal\s+information|identity|contact\s+details|billing\s+information|"
        r"name|address|phone)\b",
        msg, re.I
    ))
    if restricted and specific_info_needed:
        score += 4

    # Pattern 6: "I need/want/require your [sensitive info] asap/urgently" (5 points)
    # Example: "I need your address and dob asap"
    # This is a direct personal request for private information
    direct_need = bool(re.search(
        r"\bi\s+(need|require|want|ask).{0,50}(your|you).{0,50}(address|dob|phone|mobile|postcode|details|information)\b",
        msg, re.I
    ))
    if direct_need and field_count >= 2:
        score += 5

    # Pattern 7: "Reply/Send with your [info]" at start of message (3 points)
    # Example: "Reply with your name and address and YOU WILL RECEIVE..."
    # This is a common SMS/Twitter spam pattern
    starts_with_request = bool(re.search(r"^(reply|send|text|provide)\s+(with|us)\b", msg, re.I))
    if starts_with_request and field_count >= 2:
        score += 3

    # Pattern 8: Advance-fee/419 scam patterns (3 points) - Email-specific
    # Example: "I need your cooperation", "Can you assist", "Contact me for details"
    # These are common in email scams that request info through correspondence
    advance_fee_cues = bool(re.search(
        r"\b(seek\s+(your\s+)?cooperation|can\s+you\s+assist|need\s+(your\s+)?assistance|"
        r"require\s+(your\s+)?cooperation|business\s+proposal)\b",
        msg, re.I
    ))

    # Financial transfer/fund language (common in 419 scams)
    financial_transfer = bool(re.search(
        r"\b(transfer.{0,30}(fund|money|amount)|fund.{0,30}transfer|bank\s+account.{0,50}(transfer|details)|"
        r"sum\s+of.{0,30}(million|thousand|usd|gbp|eur))\b",
        msg, re.I
    ))

    # Contact request in financial context
    contact_financial = bool(re.search(
        r"\b(contact|reply|respond|email\s+me).{0,50}(fund|transfer|claim|bank|account|payment)\b",
        msg, re.I
    )) or bool(re.search(
        r"\b(fund|transfer|claim|bank|account|payment).{0,50}(contact|reply|respond|email\s+me)\b",
        msg, re.I
    ))

    if advance_fee_cues or (financial_transfer and contact_financial):
        score += 3

    # Pattern 9: "We need more information" in account/billing context (3 points)
    # Example: "We need more information to help us provide you with secure service"
    need_more_info = bool(re.search(
        r"\b(need|require).{0,30}(more|additional).{0,30}(information|details)\b",
        msg, re.I
    ))
    account_context = bool(re.search(
        r"\b(account|billing|payment|profile|service|identity|verification)\b",
        msg, re.I
    ))
    if need_more_info and account_context:
        score += 3

    # === DECISION: Lowered threshold to 3 for better recall ===
    # This catches:
    # - Explicit requests (score 5-6+): SMS/Twitter direct requests
    # - Advance-fee scams (score 3-6): Email 419 scams, business proposals
    # - Account info requests (score 3-5): Phishing attempts
    return score >= 3


# ----------------------------
# Basic normalisation utilities
# ----------------------------

def normalise(message: str) -> str:
    if message is None:
        return ""
    # Keep punctuation relevant for URLs/emails; collapse whitespace
    msg = str(message).lower()
    msg = msg.replace("\u2019", "'").replace("\u2018", "'")
    msg = re.sub(r"\s+", " ", msg).strip()
    return msg



def contains_url(text: str) -> bool:
    return bool(URL_REGEX.search(str(text or "")))

def contains_url_loose(text: str) -> bool:
    t = str(text or "")
    return bool(URL_REGEX.search(t) or LOOSE_URL_REGEX.search(t))

# ----------------------------
# Email preprocessing: strip quoted threads/digests
# ----------------------------



def extract_primary_email_text(text: str) -> str:
    t = str(text or "")
    # Stop at common quote markers
    m = QUOTE_CUTOFF_RE.search(t)
    if m:
        t = t[:m.start()]
    # Drop quoted lines
    lines = []
    for ln in t.splitlines():
        if ln.strip().startswith(">"):
            continue
        lines.append(ln)
    return "\n".join(lines).strip()


def _extract_sender_email(sender: str):
    if not sender:
        return None
    m = EMAIL_REGEX.search(str(sender))
    return m.group(0).lower() if m else None

def _sender_domain(sender: str):
    em = _extract_sender_email(sender)
    if not em or "@" not in em:
        return None
    return em.split("@", 1)[1].lower()

def _sender_display(sender: str) -> str:
    if not sender:
        return ""
    s = str(sender)
    return normalise(s.split("<", 1)[0])

# Build a matcher for BRAND names (exclude generic "Other"; do not rely on short ambiguous abbrs)
def _brand_patterns():
    pats = []
    for b in BRANDS.get("phishing_targets", []):
        name = (b.get("name") or "").strip()
        abbr = (b.get("abbr") or "").strip()
        if not name or name.lower() == "other":
            continue
        # match name words in order
        toks = [t for t in re.split(r"[\s,&()\-]+", name.lower()) if t and t not in {"the","and","of"}]
        if toks:
            pats.append(r"\b" + r"\s+".join(map(re.escape, toks)) + r"\b")
        # keep a small allowlist of 3-letter abbrs that are actually distinctive
        if abbr and len(abbr) >= 4:
            pats.append(r"\b" + re.escape(abbr.lower()) + r"\b")
        elif abbr and abbr.upper() in {"DHL","UPS","IRS","HMRC"}:
            pats.append(r"\b" + re.escape(abbr.lower()) + r"\b")
    return re.compile("|".join(pats), re.I) if pats else re.compile(r"a^")

_BRAND_RE = _brand_patterns()

def _domain_looks_like_brand(domain: str, brand_text: str) -> bool:
    if not domain or not brand_text:
        return False
    d = domain.lower().replace(".", "")
    # use first meaningful token from brand_text
    toks = [t for t in re.split(r"[\s,&()\-]+", brand_text.lower()) if t and t not in {"the","and","of","inc","corp","corporation","ltd","limited","llc","plc"}]
    toks = [t for t in toks if len(t) >= 3]
    return any(t.replace(".", "") in d for t in toks)

def check_impersonation(message, sender=None):

    raw = "" if message is None else str(message)

    # Treat long texts with a sender as email-like; otherwise short as SMS/social-like.
    is_email_like = sender is not None or len(raw) > 800

    text = extract_primary_email_text(raw) if is_email_like else raw
    if TECH_BULLETIN_RE.search(text):
        return False

    msg = normalise(text)
    has_link = contains_url_loose(text)
    has_phone = bool(re.search(PHONE_LIKE, text)) or bool(re.search(SHORTCODE, text))
    has_email_in_body = bool(EMAIL_REGEX.search(text))
    has_contact_word = bool(re.search(r"\b(call|dial|contact|reply|text|email|chat)\b", msg))
    contact = has_link or has_phone or has_email_in_body or has_contact_word

    # Bank/card blocked/jam: requires phone contact to stay aligned with Dataset_10191 labels
    if BANK_ACTION_RE.search(text) and BANK_CARD_RE.search(text) and has_phone and contact:
        # exclude benign-ish transactional wording (seen as non-impersonation in Dataset_10191)
        if not re.search(r"\bhas noticed\b.*\bdebit card\b.*\bused\b", text, re.I):
            return True

    # Apple ID/iCloud/Apple Support: tolerate 'Apple 1D' + noisy 'due to ... expire'
    if APPLE_CLAIM_RE.search(text):
        apple_action = bool(APPLE_ACTION_RE.search(text) or APPLE_EXPIRE_RE.search(text) or APPLE_DUE_EXPIRE_RE.search(text))
        if apple_action and (contact or has_link):
            # Keep label alignment: if it has a strict URL, this often goes to Link Click Pressure.
            if contains_url(text) and sender is None:
                # still allow if it's an obfuscated URL (no colon / spaced dot) rather than a clean URL
                if not URL_REGEX.search(text) and has_link:
                    return True
            else:
                return True

    # Bank of America 'account suspended, log in to restore' with obfuscated URLs
    if BOA_RE.search(text) and BOA_SUSP_RE.search(text) and BOA_LOGIN_RE.search(text) and BOA_RESTORE_RE.search(text) and has_link:
        return True

    # HMRC/GOVUK/IRS refund + link
    if TAX_AGENCY_RE.search(text) and REFUND_RE.search(text) and has_link:
        return True

    # Paytm FASTag / payments bank request + link
    if PAYTM_RE.search(text) and PAYTM_CTX_RE.search(text) and has_link:
        return True

    # Order cancel/delete + link
    if ORDER_CANCEL_RE.search(text) and has_link and ORDER_CTA_RE.search(text):
        return True

    # ----------------------------
    # TELECOM BRAND IMPERSONATION (added to fix annotation inconsistency)
    # ----------------------------
    # Messages claiming to be from telecom companies (Orange, Vodafone, T-Mobile, O2)
    if re.search(r"\b(orange|vodafone|tmobile|t-mobile|o2)\s+(customer|subscriber|user)", msg, re.I):
        if contact:
            return True

    # ----------------------------
    # PRIZE/LOTTERY SCAM IMPERSONATION (added to fix annotation inconsistency)
    # ----------------------------
    # Messages falsely claiming recipient has won prizes without legitimate authority
    # "You have won £1000", "You've been selected to receive"
    prize_scam = bool(re.search(
        r"\b(you\s+have\s+won|you've\s+won|you\s+are\s+a\s+winner|won\s+a|"
        r"selected\s+to\s+receive|you\s+have\s+been\s+selected|you\s+are\s+awarded|"
        r"congratulations.*won|awarded\s+a)\b",
        msg, re.I
    ))

    # Prize value/reward
    prize_value = bool(re.search(
        r"(£|$|€|gbp|usd|eur)\s*\d+|prize|award|cash|voucher|gift\s+card|winning",
        msg, re.I
    ))

    if prize_scam and prize_value and contact:
        return True

    # ----------------------------
    # Email-like impersonation (TREC_06): org claim + phishing action + contact
    # Uses sender only as a *precision guard* (e.g., suppress clear government/academic senders).
    # ----------------------------
    if sender is not None:
        sdom = (_sender_domain(sender) or "").lower()

        # Precision guard: many legitimate messages in TREC_06 come from .gov/.edu/.mil domains.
        # We avoid flagging these unless the message clearly contains phishing workflow language + links.
        if sdom.endswith(('.gov', '.gov.uk', '.mil', '.edu')) and not EMAIL_ACTION_RE.search(raw):
            return False

        org_claim_re = re.compile(
            r"\b("
            r"bank|treasury|ministry|government|customs|immigration|revenue|tax|"
            r"paypal|amazon|apple|microsoft|google|bank\s*of\s*america|bankofamerica|"
            r"hmrc|govuk|gov\.uk|irs|dhl|ups|fedex|usps|royal\s*mail"
            r")\b",
            re.I
        )

        if org_claim_re.search(raw):
            if contact and EMAIL_ACTION_RE.search(raw):
                return True

        # Authority-role impersonation (common advance-fee scams):
        # Keep it organization/role-focused and sender-driven (avoid prize/lure words).
        if re.search(r"\b(from\s+the\s+desk\s+of|office\s+of|attn\b|attention\b|"
                    r"director|chairman|vice\s+president|ambassador|attorney|barrister)\b", raw, re.I):
            if contact and (sdom in FREE_EMAIL or not sdom):
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

