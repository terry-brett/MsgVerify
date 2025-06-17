from verifiers import URLVerifier


url = "https://tinyurl.com/bdfpfyur"

verifier = URLVerifier(url)

verifier.print_evaluation_report()