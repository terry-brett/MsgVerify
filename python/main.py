from verifiers import URLVerifier


url = "https://www.google.com"

verifier = URLVerifier(url, True)

print(verifier.is_scheme_secured())
print(verifier.is_domain_blocklisted())
print(verifier.is_url_ip_based())


