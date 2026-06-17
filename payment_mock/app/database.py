# Simulación de un extracto bancario (Ledger) con transferencias ya recibidas en la cuenta del hotel.
LEDGER = {
    "NEQ-111": {
        "amount": 320000.00, # Pago exacto para el Área Social
        "date": "2026-06-14T10:00:00Z",
        "sender": {"name": "Julio Amaya", "documentId": "100200300", "phone": "3001234567", "bank": "NEQUI"}
    },
    "DAV-222": {
        "amount": 1250000.00, # Pago exacto para la Suite (5 noches)
        "date": "2026-06-14T11:30:00Z",
        "sender": {"name": "Maria Gomez", "documentId": "100200301", "phone": "3111111111", "bank": "DAVIPLATA"}
    },
    "NEQ-333": {
        "amount": 10000.00, # PAGO INCOMPLETO (Simulación de fraude)
        "date": "2026-06-14T12:15:00Z",
        "sender": {"name": "Carlos Perez", "documentId": "100200302", "phone": "3222222222", "bank": "NEQUI"}
    },
    "DAV-444": {
        "amount": 500000.00,
        "date": "2026-06-14T14:20:00Z",
        "sender": {"name": "Ana Torres", "documentId": "100200303", "phone": "3333333333", "bank": "DAVIPLATA"}
    },
    "NEQ-555": {
        "amount": 250000.00,
        "date": "2026-06-14T16:45:00Z",
        "sender": {"name": "Luis Rojas", "documentId": "100200304", "phone": "3444444444", "bank": "NEQUI"}
    }
}
