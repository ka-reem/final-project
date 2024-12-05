const express = require('express');
const cors = require('cors');
const fetch = require('node-fetch');
const path = require('path');
const config = require('./config.js');

// Verify config is loaded correctly
console.log('Config loaded:', {
    accountId: config.account_id,
    hasToken: !!config.api_token
});

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.static('.')); // Serve static files

// Add health check endpoint
app.get('/health', (req, res) => {
    res.json({ status: 'ok' });
});

app.post('/generate-image', async (req, res) => {
    try {
        const { prompt } = req.body;
        if (!prompt) {
            return res.status(400).json({ error: 'Prompt is required' });
        }

        console.log('Making request with prompt:', prompt);

        const response = await fetch(`https://api.cloudflare.com/client/v4/accounts/${config.account_id}/ai/run/@cf/bytedance/stable-diffusion-xl-lightning`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${config.api_token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                prompt: prompt
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Cloudflare API Error:', errorText);
            throw new Error(`Cloudflare API error: ${response.status}`);
        }

        // Get the response as a buffer directly
        const imageBuffer = await response.buffer();
        
        // Set correct content type and send buffer
        res.set('Content-Type', 'image/png');
        res.send(imageBuffer);
        
    } catch (error) {
        console.error('Error:', error);
        res.status(500).json({ error: error.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
    console.log('Make sure to run the frontend on a different port');
});