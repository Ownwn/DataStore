import { BrowserRouter, Route, Routes } from "react-router-dom";
import {Home} from "./routes/home.tsx";
import {NotFound} from "./routes/notfound.tsx";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Home/>}/>
                <Route path="*" element={<NotFound/>}/>
            </Routes>
        </BrowserRouter>
    );
}

export default App;
