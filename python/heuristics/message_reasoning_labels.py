class MessageReasoningLabels:
    def __init__(self, message):
        self.message = message

    def check_spelling_and_formatting(self):
        return ""  # add "Grammatical Errors/Poor Formatting" label

    def check_impersonation(self):
        # the sender/message pretends to be another company e.g. Amazon/Paypal
        # we have a json top_impersonated_brands.json you can use here
        return "" # add "Impersonation" label

    def check_marketing(self):
        return "" # add "Marketing" label

    def check_adult_content(self):
        return "" # add "Adult content" label

    def check_urgency(self):
        return "" # add "Urgency/Intimidation" label

    def contains_links(self):
        return ""  # add "Link Click Pressure" label

    def is_asking_for_financial_or_personal_information(self):
        return "" # add "Financial/Personal Information Request" label

    def check_too_good_to_be_true(self):
        return "" # add "Too Good To Be True" label

    def check_credential_verification_request(self):
        return "" # add "Credential Verification Request" label

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