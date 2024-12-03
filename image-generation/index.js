const url = `https://api.cloudflare.com/client/v4/accounts/${config.account_id}/ai/run/${model}`;

console.log(url);

fetch(url, {
    method: 'POST',
    headers: headers,
    body: JSON.stringify({ prompt: prompt })
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));