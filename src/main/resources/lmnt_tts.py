
import asyncio
from lmnt.api import Speech

async def main():
  async with Speech() as speech:
    synthesis = await speech.synthesize('Hello world. How are you doing today', 'lily')
  with open('hello.mp3', 'wb') as f:
    f.write(synthesis['audio'])

asyncio.run(main())