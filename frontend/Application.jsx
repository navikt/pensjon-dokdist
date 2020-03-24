import React, { useReducer } from 'react';
import {fetch} from 'whatwg-fetch'
import {
    BrowserRouter as Router,
    Switch,
    Route,
    useParams,
    useLocation
} from "react-router-dom";
import {Sidetittel, Systemtittel} from "nav-frontend-typografi";
import { Hovedknapp } from 'nav-frontend-knapper';
import { AlertStripeSuksess, AlertStripeFeil } from 'nav-frontend-alertstriper';

async function distribuerJournalpost({ id, status }) {
    console.log('distribuer', id);
    console.log('status', status);
    const response = await fetch(`/api/journalpost/${id}/send`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-type': "application/json"
            },
        body:JSON.stringify({
            journalpostId: id,
            status
        })
    });

    if (response.status === 200) {
        return;
    } else {
        throw new Error('Noe gikk galt');
    }
}

const initialState = {
    status: 'INITIAL'
};
function reducer(state, action) {
    console.log(action.type, action);
    switch (action.type) {
        case 'SUBMIT':
            return {
                ...state,
                status: 'SUBMITTING'
            };
        case 'SUBMIT_SUCCESS':
            return {
                ...state,
                status: 'SUBMITTED'
            };
        case 'SUBMIT_ERROR':
            return {
                ...state,
                status: 'SUBMIT_ERROR'
            };
        default:
            console.log('Unknown type', action);
            return state;
    }
}

function Journalpost() {
    const [state, dispatch] = useReducer(reducer, initialState);
    const location = useLocation();

    const queryParams = new URLSearchParams(location.search);
    const status = queryParams.get('status');

    const { id } = useParams();

    const handleSubmit = async () => {
        try {
            dispatch({ type: 'SUBMIT' });
            await distribuerJournalpost({ id, status });
            dispatch({ type: 'SUBMIT_SUCCESS' });
        } catch (err) {
            dispatch({ type: 'SUBMIT_ERROR' });
        }
    };

    const inProgress = state.status === 'SUBMITTING';
    const label = inProgress ? 'Send brevet...' : 'Send brevet';
    const shouldShowButton = state.status === 'INITIAL' || inProgress;
    const isSuccess = state.status === 'SUBMITTED';
    const isError = state.status === 'SUBMIT_ERROR';

    return (
        <div>
            <Systemtittel>Journalpost med ID: {id}</Systemtittel>
            {shouldShowButton && <Hovedknapp onClick={handleSubmit} disabled={inProgress} spinner={inProgress}>{label}</Hovedknapp>}
            {isSuccess && <AlertStripeSuksess>Journalpost er nå sendt.</AlertStripeSuksess>}
            {isError && <AlertStripeFeil>Noe gikk visst galt!</AlertStripeFeil>}
        </div>
    );
}

export default function Application() {
    return (
        <Router>
            <Sidetittel>Distribusjon av brevet</Sidetittel>
            <Switch>
                <Route path="/journalpost/:id">
                    <Journalpost/>
                </Route>
                <Route path="/">
                    <div>Forside</div>
                </Route>
            </Switch>
        </Router>
    );
}
