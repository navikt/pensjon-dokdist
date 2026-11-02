import * as React from 'react';
import { createRoot } from 'react-dom/client';
import {lazy, StrictMode, Suspense} from 'react';

const App = lazy(() => import("./App"));

const rootElement = document.getElementById("root");
const root = createRoot(rootElement);

root.render(
    <StrictMode>
        <Suspense fallback={null}>
            <App/>
        </Suspense>
    </StrictMode>,
);
