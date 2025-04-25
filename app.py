from flask import Flask, request, jsonify
import openai
import os
from dotenv import load_dotenv

load_dotenv()
openai.api_key = os.getenv("OPENAI_API_KEY")

app = Flask(__name__)

@app.route('/generate-event-plan', methods=['POST'])
def generate_event_plan():
    data = request.get_json()
    
    event_type = data.get('event_type', 'Birthday')
    location = data.get('location', 'New York')
    date = data.get('date', '2025-05-01')
    guest_count = data.get('guest_count', 50)
    budget = data.get('budget', 5000)
    cuisine = ", ".join(data.get('cuisine_preferences', []))
    style = data.get('style', 'Casual')
    special_requests = data.get('special_requests', 'None')
    
    prompt = f"""
    Generate a personalized event plan for the following:
    - Event type: {event_type}
    - Location: {location}
    - Date: {date}
    - Guest count: {guest_count}
    - Budget: ${budget}
    - Cuisine preferences: {cuisine}
    - Style: {style}
    - Special requests: {special_requests}

    Include:
    1. Event title
    2. 3 venue suggestions with reasoning
    3. Catering plan
    4. Decoration and theme ideas
    5. Full schedule
    6. Bonus tips or notes
    """

    try:
        response = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "user", "content": prompt}
            ],
            max_tokens=1000,
            temperature=0.7
        )
        plan = response['choices'][0]['message']['content']
        return jsonify({
            "success": True,
            "event_plan": plan
        })

    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500

if __name__ == '__main__':
    app.run(debug=True)