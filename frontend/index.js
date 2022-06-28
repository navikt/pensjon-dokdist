import * as React from 'react';
import ReactDOM, { createRoot } from 'react-dom/client';
import {StrictMode} from 'react';
import 'regenerator-runtime';
import 'core-js';
import {BrowserRouter as Router, Route, Routes} from "react-router-dom";
import Layout from "./Layout";
import Journalpost from "./Journalpost";

const rootElement = document.getElementById("root");
const root = createRoot(rootElement);

root.render(
    <StrictMode>
        <Router>
            <Routes>
                <Route path="/" element={<Layout />}>
                    <Route index element={<h1>teeee</h1>} />
                </Route>
                <Route path="journalpost">
                    <Route path=":id" element={<Journalpost/>} />
                </Route>
            </Routes>
        </Router>
    </StrictMode>,
);
