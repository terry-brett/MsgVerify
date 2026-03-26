import re
from helpers import *

class MessageReasoningLabels:
    def __init__(self, message, sender = None):
        self.raw_message = message #for spell check function so it can identify nouns
        self.message = normalise(message)
        self.sender = sender # for emails we should also pass a sender to check TLD
        self.labels = []

    def check_spelling_and_formatting(self):
        if has_spelling_error(self.raw_message) or self._check_poor_formatting():
            # add "Grammatical Errors/Poor Formatting" label
            self.labels.append("Grammatical Errors/Poor Formatting")
        

    def check_impersonation(self):
        if check_impersonation(self.message, self.sender):
            self.labels.append("Impersonation")

    def check_marketing(self):
        if has_marketing_patterns(self.message):
            self.labels.append("Marketing")

    def check_adult_content(self):
        if has_adult_content_patterns(self.message):
            self.labels.append("Adult Content")

    def check_urgency(self):
        if has_urgency_or_intimidation_patterns(self.message):
            self.labels.append("Urgency/Intimidation")

    def contains_links(self):
        if contains_url(self.message):
            self.labels.append("Link Click Pressure")

    def is_asking_for_financial_or_personal_information(self):
        if asks_for_financial_or_personal_info(self.message):
            self.labels.append("Financial or Personal Information")

    def check_too_good_to_be_true(self):
        if has_too_good_to_be_true_patterns(self.message):
            self.labels.append("Too Good To Be True")

    def check_credential_verification_request(self):
        if has_credential_verification_patterns(self.message):
            self.labels.append("Credential Verification Request")

    def _check_poor_formatting(self):
        # detects excessive capitalization (common in spam)
        caps_ratio = sum(1 for c in self.raw_message if c.isupper()) / (len(self.raw_message) + 1)
        return caps_ratio > 0.3 or bool(re.search(r"[a-z][A-Z][a-z][A-Z]", self.raw_message))

    def add_reasoning_labels(self):
        self.check_spelling_and_formatting()
        self.check_impersonation()
        self.check_marketing()
        self.check_adult_content()
        self.check_urgency()
        self.contains_links()
        self.is_asking_for_financial_or_personal_information()
        self.check_too_good_to_be_true()
        self.check_credential_verification_request()

        # return set of reasoning labels or uncategorized spam
        return list(set(self.labels)) if self.labels else ["Uncategorized Spam"]

'''
"Impersonation": r"(amazon|mygov|wells fargo|paypal|hmrc|netflix|apple|microsoft|delivery|o2|vodafone|bank)",
"Urgency": r"(urgent|immediately|action required|expires|final notice|within \d+ hours|hurry|now1!)",
"Intimidation": r"(locked|suspended|illegal|police|unauthorized|blocked|frozen|security risk)",
"Financial information request": r"(bank account|sort code|iban|credit card|routing number|account number)",
"Sensitive Information Request": r"(valid name|house no|postcode|dob|date of birth|ssn)",
"Too Good To Be True": r"(refund|won|lottery|prize|compensation|gift card|cash|free|selected)",
"Fake Technical Language": r"(ssl error|database breach|protocol|security certificate|server bypass|error code)",
"Generic Greeting": r"(dear customer|dear user|valued member|dear sir/madam|hey there darling)",
"Unusual Account Activity Claim": r"(suspicious activity|unusual login|new device detected|trying 2 contact u)",
"Credential Verification Request": r"(verify your password|update credentials|confirm login)",
"Payment Request": r"(pay now|wire transfer|payment required|overdue|outstanding balance)",
"Bypassing Official Channels": r"(reply to this text|text back|call now|reply with)",
"Emotional Manipulation": r"(help me|emergency|stuck|stranded|worried|family)",
"Adult Content / Solicitation": r"(shag|sextext|suzy|dating|hot girls|meet me)",
'''