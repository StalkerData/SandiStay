from pydantic import BaseModel
from typing import Optional

class SenderInfo(BaseModel):
    name: str
    documentId: str
    phone: str
    bank: str

class BankStatementResponse(BaseModel):
    found: bool
    receiptId: Optional[str] = None
    transferDate: Optional[str] = None
    amount: Optional[float] = None
    currency: Optional[str] = None
    sender: Optional[SenderInfo] = None
    destinationAccount: Optional[str] = None
    message: str
