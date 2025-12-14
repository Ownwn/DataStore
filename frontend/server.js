import express from 'express';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { createProxyMiddleware } from 'http-proxy-middleware';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();


app.use('/api', createProxyMiddleware({
    target: 'http://127.0.0.1:32200/api',
    changeOrigin: true,
}));


app.use(express.static(join(__dirname, 'dist')));

app.get('*foo', (req, res) => {
    res.sendFile(join(__dirname, 'dist', 'index.html'));
});

app.listen(32000, () => {
    console.log("Server started")
});