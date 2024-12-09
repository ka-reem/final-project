import asyncio
import sys
import urllib.parse
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

try:
    from lmnt.api import Speech
except ImportError:
    print("Error: LMNT package not found. Please install it using:")
    print("pip3 install lmnt")
    sys.exit(1)

async def generate_speech(text, output_file='output.mp3'):
    try:
        decoded_text = urllib.parse.unquote(text)
        print(f"Generating speech for: {decoded_text}")
        print(f"Output file: {output_file}")
        
        api_key = os.getenv('LMNT_API_KEY')
        if not api_key:
            raise ValueError("LMNT_API_KEY not found in environment variables")
            
        async with Speech(api_key=api_key) as speech:
            # synthesis = await speech.synthesize(decoded_text, 'lily')
            synthesis = await speech.synthesize(decoded_text, '6bc9c06a-8e4a-4739-8b31-259f937f71d4')
            
        with open(output_file, 'wb') as f:
            f.write(synthesis['audio'])
            
        print("Speech generation complete")
        return True
    except Exception as e:
        print(f"Error generating speech: {str(e)}")
        return False

def text_to_speech(text, output_file='output.mp3'):
    asyncio.run(generate_speech(text, output_file))

# Allow command line usage
if __name__ == "__main__":
    if len(sys.argv) > 1:
        text = sys.argv[1]
        output_file = sys.argv[2] if len(sys.argv) > 2 else 'output.mp3'
        text_to_speech(text, output_file)