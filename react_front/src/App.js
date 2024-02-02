import logo from './logo.svg';
import './App.css';
import React from 'react';
import { toastNotice } from './ToastrConfig';

function App() {

  const handleAlert = () => {
    toastNotice('경고 창 띄우기.');
  };

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
      </header>
      <button onClick={handleAlert}>경고 표시</button>
    </div>
  );
}

export default App;