from verifiers import URLVerifier

def main():
    url = "https://uk-covid-19.webredirect.org/to"
    check_url(url)


def check_url(url: str):
    verifier = URLVerifier(url)

    probs = verifier.get_ml_prediction()

    label = "Phishing" if (float(probs[0]) > 0.5) else "Safe"

    probs = round((float(probs[0]) * 100), 2)

    print("URL: ", url)
    print("Prediction: ", label)
    print("Probability of URL being a Phished URL: ", probs, " %")



if __name__ == "__main__":
    main()