from apscheduler.schedulers.background import BackgroundScheduler
from app.api.train import execute_training_pipeline
import logging

logger = logging.getLogger(__name__)

def start_scheduler():
    scheduler = BackgroundScheduler()
    scheduler.add_job(execute_training_pipeline, 'cron', hour=2, minute=0)
    scheduler.start()
    logger.info("Background scheduler started and job added.")
