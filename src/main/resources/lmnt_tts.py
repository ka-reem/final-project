import asyncio
import sys
import urllib.parse

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
        
        async with Speech() as speech:
            synthesis = await speech.synthesize(decoded_text, 'lily')
            
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