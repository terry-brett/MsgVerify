from dataclasses import dataclass
from typing import List

@dataclass
class Reason:
    reason : str

@dataclass
class URLVerifierResponse:
    score : int
    reasons : List[Reason]

