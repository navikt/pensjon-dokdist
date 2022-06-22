import * as React from 'react';
import { createRoot } from 'react-dom/client';
import {StrictMode} from 'react';
import 'regenerator-runtime';
import 'core-js';
import Application from "./Application";
import {BrowserRouter as Router, Route, Routes} from "react-router-dom";


const rootElement = document.getElementById("root");
const root = createRoot(rootElement);

root.render(
    <StrictMode>
        <Router>
            <Routes>
                <Route path="/" element={<div>test</div>} />
                <Route path="about" element={<div>test2</div>} />
            </Routes>
        </Router>
    </StrictMode>,
);
