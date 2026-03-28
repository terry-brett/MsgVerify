from classifiers.URLBinaryClassifier import Classifier

url = "https://uk-covid-19.webredirect.org/to"

classifier = Classifier()
probs = classifier.predict(url)
label = "Phishing" if (float(probs[0]) > 0.5) else "Safe"

probs = round((float(probs[0]) * 100), 2)

print("URL: ", url)
print("Prediction: ", label)
print("Probability of URL being a Phished URL: ", probs, " %")