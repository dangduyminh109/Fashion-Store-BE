from fastapi import APIRouter, BackgroundTasks
from app.services.data_fetcher import fetch_orders
from app.services.apriori import train_apriori
import logging

logger = logging.getLogger(__name__)

router = APIRouter()

def execute_training_pipeline():
    try:
        logger.info("Starting background training pipeline...")
        dataset = fetch_orders()
        train_apriori(dataset)
        logger.info("Training pipeline completed.")
    except Exception as e:
        logger.error(f"Training pipeline failed: {e}", exc_info=True)

@router.post("/train")
async def trigger_training(background_tasks: BackgroundTasks):
    background_tasks.add_task(execute_training_pipeline)
    return {"message": "Training job started in the background."}
