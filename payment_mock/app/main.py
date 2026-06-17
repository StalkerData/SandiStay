from fastapi import FastAPI, Query
from .models import BankStatementResponse
from .services import process_verification

app = FastAPI(title="SandiStay Bank Mock API", description="Simulador de Conciliación Bancaria")

@app.get("/api/v1/bank/statement/verify", response_model=BankStatementResponse)
def verify_payment(receiptId: str = Query(..., description="Número de comprobante de la transferencia")):
    return process_verification(receiptId)
