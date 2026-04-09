from fastapi import APIRouter, Query
from app.models.database import recommend_rules
from typing import List

router = APIRouter()

@router.get("/recommend")
def get_recommendations(items: str = Query(..., description="Comma separated list of product IDs"), limit: int = 5):
    try:
        input_items = [int(x.strip()) for x in items.split(",") if x.strip().isdigit()]
        if not input_items:
            return {"recommendations": []}
            
        input_set = set(input_items)
        
        query = {"antecedent": {"$in": input_items}}
        cursor = recommend_rules.find(query)
        
        valid_rules = []
        for rule in cursor:
            antecedent_set = set(rule['antecedent'])
            if antecedent_set.issubset(input_set):
                valid_rules.append(rule)
                
        valid_rules = [r for r in valid_rules if r['consequent'] not in input_set]
                
        # 5. Rank by confidence DESC then lift DESC
        valid_rules.sort(key=lambda x: (x['confidence'], x['lift']), reverse=True)
        
        recommended_items = []
        seen = set()
        
        for rule in valid_rules:
            consequent = rule['consequent']
            if consequent not in seen:
                seen.add(consequent)
                recommended_items.append(consequent)
                if len(recommended_items) >= limit:
                    break
                    
        return {"recommendations": recommended_items}
        
    except Exception as e:
        print(f"Error in recommendation logic: {e}")
        return {"recommendations": []}
