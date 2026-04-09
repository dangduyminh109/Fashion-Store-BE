import logging

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    handlers=[logging.StreamHandler()],
    force=True
)

from fastapi import FastAPI
from app.api import recommend, train
from app.jobs.scheduler import start_scheduler

logger = logging.getLogger(__name__)

app = FastAPI(title="Recommendation Service", version="1.0")

app.include_router(recommend.router, prefix="/api", tags=["recommend"])
app.include_router(train.router, prefix="/api", tags=["train"])

@app.on_event("startup")
async def startup_event():
    start_scheduler()

@app.get("/")
def root():
    return {"message": "Recommendation Service is running"}
