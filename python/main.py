from verifiers import URLVerifier

url = "https://www.facebook.com"

verifier = URLVerifier(url)

probs = verifier.get_ml_prediction()

label = "Phishing" if (float(probs[0]) > 0.5) else "Safe"

probs = round((float(probs[0]) * 100), 2)

print("URL: ", url)
print("Prediction: ", label)
print("Probability of URL being a Phished URL: ", probs, " %")