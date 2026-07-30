import * as React from 'react';
import { createRoot } from 'react-dom/client';
import {StrictMode} from 'react';
import {BrowserRouter as Router, Route, Routes} from "react-router";
import Journalpost from "./Journalpost";

const rootElement = document.getElementById("root");
const root = createRoot(rootElement);

root.render(
    <StrictMode>
        <Router>
            <Routes>
                <Route path="journalpost">
                    <Route path=":id" element={<Journalpost/>} />
                </Route>
            </Routes>
        </Router>
    </StrictMode>,
);
