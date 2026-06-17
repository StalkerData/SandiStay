import time
import datetime
from .models import BankStatementResponse, SenderInfo
from .database import LEDGER

def process_verification(receipt_id: str) -> BankStatementResponse:
    # 1. Simular caída de red o lentitud extrema del banco
    if receipt_id.startswith("DEMORA"):
        time.sleep(5)
        return BankStatementResponse(
            found=False, 
            message="Timeout: No se pudo conectar con la red ACH en este momento."
        )

    # 2. LÓGICA DINÁMICA (El truco inteligente para QA)
    # Si empieza con EXACTO- o FRAUDE-, extraemos el número y generamos una respuesta al vuelo
    if receipt_id.startswith("EXACTO-") or receipt_id.startswith("FRAUDE-"):
        try:
            amount_str = receipt_id.split("-")[1]
            amount = float(amount_str)
        except ValueError:
            amount = 0.0
            
        return BankStatementResponse(
            found=True,
            receiptId=receipt_id,
            transferDate=datetime.datetime.utcnow().isoformat() + "Z",
            amount=amount,
            currency="COP",
            sender=SenderInfo(
                name="Cliente QA Dinámico",
                documentId="999999999",
                phone="3000000000",
                bank="NEQUI" if "NEQUI" in receipt_id.upper() else "DAVIPLATA"
            ),
            destinationAccount="HOTEL-SANDISTAY-MAIN",
            message="Transferencia encontrada (Generada dinámicamente)."
        )

    # 3. Buscar el comprobante en nuestro libro mayor estático (Ledger)
    transaction = LEDGER.get(receipt_id)

    # Si existe en el Ledger, devolvemos los datos reales de la transferencia
    if transaction:
        return BankStatementResponse(
            found=True,
            receiptId=receipt_id,
            transferDate=transaction["date"],
            amount=transaction["amount"],
            currency="COP",
            sender=SenderInfo(**transaction["sender"]),
            destinationAccount="HOTEL-SANDISTAY-MAIN",
            message="Transferencia encontrada en el extracto bancario."
        )

    # 4. Si no existe ni es dinámico (Comprobante falso)
    return BankStatementResponse(
        found=False,
        message="Comprobante no encontrado. Verifique el número e intente nuevamente."
    )
