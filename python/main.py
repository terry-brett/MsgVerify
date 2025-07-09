from verifiers import URLVerifier

url = "https://l.wl.co/l?u=https://cpcalendars.64-226-105-5.cprapid.com/short/?Verification=a97trrybrett@yahoo.co.uk"



verifier = URLVerifier(url)

probs = verifier.get_ml_prediction()

label = "Phishing" if (float(probs[0]) > 0.5) else "Safe"

probs = round((float(probs[0]) * 100), 2)

print("URL: ", url)
print("Prediction: ", label)
print("Probability of URL being a Phished URL: ", probs, " %")