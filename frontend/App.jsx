import React from 'react';
import {BrowserRouter as Router, Route, Routes} from "react-router";
import Journalpost from "./Journalpost";

export default function App() {
    return (
        <Router>
            <Routes>
                <Route path="journalpost">
                    <Route path=":id" element={<Journalpost/>} />
                </Route>
            </Routes>
        </Router>
    );
}
