import * as React from 'react';
import ReactDOM from 'react-dom';
import 'regenerator-runtime';
import 'core-js';

import Application from './Application';


const el = document.createElement('div');
document.body.appendChild(el);

ReactDOM.render(React.createElement(Application), el);
