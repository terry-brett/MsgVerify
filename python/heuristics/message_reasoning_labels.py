import re

class MessageReasoningLabels:
    def __init__(self, message):
        self.message = message

    def check_spelling_and_formatting(self):
        # TODO: Yash - use a spell check for English and check for poor formatting e.g. hElLo
        # ive added helper function _check_poor_formatting

        return ""  # add "Grammatical Errors/Poor Formatting" label

    def check_impersonation(self):
        # the sender/message pretends to be another company e.g. Amazon/Paypal
        # we have a json top_impersonated_brands.json you can use here
        # TODO: Yash - for emails check TLD against the json, we have full company names and abbreviations

        return "" # add "Impersonation" label

    def check_marketing(self):
        return "" # add "Marketing" label

    def check_adult_content(self):
        return "" # add "Adult content" label

    def check_urgency(self):
        return "" # add "Urgency/Intimidation" label

    def contains_links(self):
        # TODO: Yash - check if message contains links

        return ""  # add "Link Click Pressure" label

    def is_asking_for_financial_or_personal_information(self):
        return "" # add "Financial/Personal Information Request" label

    def check_too_good_to_be_true(self):
        return "" # add "Too Good To Be True" label

    def check_credential_verification_request(self):
        return "" # add "Credential Verification Request" label

    def _check_poor_formatting(self):
        # detects excessive capitalization (common in spam)
        caps_ratio = sum(1 for c in self.message if c.isupper()) / (len(self.message) + 1)
        return caps_ratio > 0.3 or bool(re.search(r"[a-z][A-Z][a-z][A-Z]", self.message))

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