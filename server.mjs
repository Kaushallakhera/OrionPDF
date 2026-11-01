import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';

const PORT = Number(process.env.PORT || process.env.DEFAULT_APP_PORT || 3000);

const htmlPage = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <title>Orion PDF Reader</title>
  <style>
    :root {
      --bg: #121010;
      --surface: #1E1B1B;
      --surface-high: #282323;
      --surface-pill: #322C2B;
      --primary: #D99B93;
      --on-primary: #421613;
      --pro-bg: #6B221F;
      --pro-text: #F3C6C1;
      --text: #ECE6E4;
      --muted: #9C9390;
      --divider: #2E2828;
    }
    body.light-theme {
      --bg: #FAF6F4;
      --surface: #EFE8E4;
      --surface-high: #E4DAD5;
      --surface-pill: #D6C9C3;
      --primary: #8C3B34;
      --on-primary: #FFFFFF;
      --text: #211B1A;
      --muted: #695E5B;
      --divider: #DDD2CC;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
    body {
      background: var(--bg);
      color: var(--text);
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      height: 100vh;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      user-select: none;
    }
    .app-shell {
      max-width: 480px;
      width: 100%;
      height: 100vh;
      margin: 0 auto;
      display: flex;
      flex-direction: column;
      position: relative;
      background: var(--bg);
      box-shadow: 0 0 40px rgba(0,0,0,0.6);
      overflow: hidden;
    }
    /* Top Bar */
    .top-bar {
      display: flex;
      align-items: center;
      padding: 14px 16px;
      gap: 10px;
      background: var(--bg);
      z-index: 10;
    }
    .top-title {
      flex: 1;
      font-size: 22px;
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .icon-btn {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      border: none;
      background: transparent;
      color: var(--text);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 19px;
      transition: background 0.15s;
    }
    .icon-btn:hover { background: rgba(255,255,255,0.08); }
    .search-bar {
      padding: 0 16px 12px;
      display: none;
    }
    .search-bar.open { display: flex; gap: 8px; align-items: center; }
    .search-input {
      flex: 1;
      background: var(--surface);
      border: 1px solid var(--divider);
      color: var(--text);
      padding: 10px 16px;
      border-radius: 24px;
      font-size: 14px;
      outline: none;
    }
    /* Main Scroll Area */
    .main-content {
      flex: 1;
      overflow-y: auto;
      padding-bottom: 24px;
    }
    .section-label {
      font-size: 16px;
      font-weight: 500;
      padding: 6px 16px 12px;
    }
    /* Continue Reading Carousel */
    .carousel {
      display: flex;
      gap: 12px;
      overflow-x: auto;
      padding: 0 16px 12px;
      scrollbar-width: none;
    }
    .carousel::-webkit-scrollbar { display: none; }
    .continue-card {
      min-width: 300px;
      background: var(--surface);
      border-radius: 16px;
      padding: 14px;
      display: flex;
      gap: 14px;
      align-items: center;
      cursor: pointer;
      border: 1px solid rgba(255,255,255,0.04);
    }
    .pdf-thumb {
      width: 54px;
      height: 74px;
      border-radius: 7px;
      background: #F4F5F7;
      color: #1A2232;
      padding: 5px;
      font-size: 5px;
      line-height: 1.25;
      overflow: hidden;
      flex-shrink: 0;
      border: 1px solid rgba(255,255,255,0.15);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
    .pdf-thumb-header {
      background: #1D4EA6;
      color: #fff;
      padding: 2px 3px;
      font-weight: 700;
      border-radius: 2px;
    }
    .continue-info { flex: 1; overflow: hidden; }
    .continue-title {
      font-size: 15.5px;
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .continue-meta {
      font-size: 12.5px;
      color: var(--muted);
      margin-top: 4px;
    }
    .progress-row {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-top: 14px;
    }
    .progress-track {
      flex: 1;
      height: 4px;
      background: var(--surface-pill);
      border-radius: 4px;
      overflow: hidden;
    }
    .progress-fill {
      height: 100%;
      background: var(--primary);
      border-radius: 4px;
    }
    .progress-pct {
      font-size: 12px;
      color: var(--muted);
    }
    /* Sort & Filter Bar */
    .toolbar-row {
      display: flex;
      align-items: center;
      padding: 6px 12px;
    }
    .sort-btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: transparent;
      border: none;
      color: var(--text);
      font-size: 15px;
      font-weight: 500;
      padding: 8px;
      border-radius: 8px;
      cursor: pointer;
    }
    /* PDF List Rows */
    .pdf-row {
      display: flex;
      align-items: center;
      padding: 10px 8px 10px 16px;
      gap: 16px;
      cursor: pointer;
      transition: background 0.12s;
    }
    .pdf-row:hover { background: rgba(255,255,255,0.04); }
    .pdf-row-info { flex: 1; overflow: hidden; }
    .pdf-row-title {
      font-size: 16px;
      font-weight: 400;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .pdf-row-sub {
      font-size: 13.5px;
      color: var(--muted);
      margin-top: 4px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    /* Grid View */
    .pdf-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
      padding: 8px 16px;
    }
    .pdf-grid-card {
      background: var(--surface);
      border-radius: 14px;
      padding: 12px;
      cursor: pointer;
    }
    /* Segmented Pill Switcher (Folders | Authors) */
    .segmented-wrap {
      display: flex;
      align-items: center;
      padding: 6px 16px 12px;
      gap: 10px;
    }
    .segmented-pill {
      flex: 1;
      height: 46px;
      background: var(--surface);
      border-radius: 999px;
      padding: 4px;
      display: flex;
    }
    .seg-btn {
      flex: 1;
      border: none;
      background: transparent;
      color: var(--muted);
      border-radius: 999px;
      font-size: 14px;
      font-weight: 500;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      cursor: pointer;
    }
    .seg-btn.active {
      background: #A89E96;
      color: #1C1817;
    }
    .folder-row {
      display: flex;
      align-items: center;
      padding: 11px 16px;
      gap: 16px;
      cursor: pointer;
    }
    .folder-row:hover { background: rgba(255,255,255,0.04); }
    .folder-icon-box {
      width: 50px;
      height: 50px;
      border-radius: 14px;
      background: var(--surface);
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--muted);
      font-size: 20px;
    }
    /* Bottom Navigation */
    .bottom-nav {
      display: flex;
      background: var(--surface);
      padding: 8px 6px 12px;
      border-top: 1px solid rgba(255,255,255,0.04);
    }
    .nav-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      cursor: pointer;
      color: var(--muted);
      font-size: 12px;
    }
    .nav-pill {
      width: 62px;
      height: 32px;
      border-radius: 999px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
    }
    .nav-item.active { color: var(--text); font-weight: 500; }
    .nav-item.active .nav-pill { background: var(--surface-pill); }

    /* PDF READER VIEW */
    .reader-screen {
      position: absolute;
      inset: 0;
      background: #141111;
      z-index: 30;
      display: none;
      flex-direction: column;
    }
    .reader-screen.open { display: flex; }
    .reader-viewport {
      flex: 1;
      overflow-y: auto;
      position: relative;
      background: #CFD3D6;
      padding: 8px 0;
      transition: filter 0.2s, background 0.2s;
    }
    .pdf-page-sheet {
      width: 95%;
      margin: 0 auto 12px;
      background: #F5F6F8;
      color: #161C28;
      border-radius: 4px;
      padding: 16px;
      position: relative;
      box-shadow: 0 2px 10px rgba(0,0,0,0.22);
      transition: transform 0.2s;
    }
    .annotation-canvas {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
      z-index: 5;
    }
    .annotation-canvas.drawing {
      pointer-events: auto;
      cursor: crosshair;
    }
    /* Floating Left Sidebar Pill (Screenshot 4 + Integrated Annotation Tools) */
    .floating-sidebar {
      position: absolute;
      left: 14px;
      bottom: 78px;
      z-index: 40;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .sidebar-pill {
      width: 52px;
      background: rgba(32, 28, 28, 0.94);
      border: 1px solid rgba(255,255,255,0.09);
      border-radius: 28px;
      padding: 8px 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      box-shadow: 0 6px 20px rgba(0,0,0,0.45);
    }
    .side-btn {
      width: 38px;
      height: 38px;
      border-radius: 50%;
      border: none;
      background: transparent;
      color: #BDB6B3;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 17px;
      position: relative;
    }
    .side-btn.active {
      background: rgba(217, 155, 147, 0.24);
      color: var(--primary);
    }
    .color-dot {
      width: 20px;
      height: 20px;
      border-radius: 50%;
      border: 2px solid #fff;
    }
    /* Reader Bottom Bar */
    .reader-bottom-bar {
      background: #1A1717;
      display: flex;
      align-items: center;
      justify-content: space-around;
      padding: 10px 8px 14px;
      border-top: 1px solid rgba(255,255,255,0.06);
    }
    .page-counter-pill {
      background: #262222;
      color: #D8D0CD;
      padding: 10px 20px;
      border-radius: 999px;
      font-size: 15px;
      font-weight: 500;
      cursor: pointer;
    }
    /* Bottom Sheets */
    .sheet-backdrop {
      position: absolute;
      inset: 0;
      background: rgba(0,0,0,0.62);
      z-index: 60;
      display: none;
      align-items: flex-end;
    }
    .sheet-backdrop.open { display: flex; }
    .bottom-sheet {
      width: 100%;
      max-height: 88vh;
      overflow-y: auto;
      background: #1F1C1C;
      color: #ECE6E4;
      border-radius: 28px 28px 0 0;
      padding: 12px 22px 26px;
    }
    .sheet-handle {
      width: 38px;
      height: 4px;
      background: #5C5553;
      border-radius: 4px;
      margin: 0 auto 16px;
    }
    /* Reading Themes 2x4 Grid (Screenshot 3) */
    .theme-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
      margin: 16px 0;
    }
    .theme-swatch-wrap {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      position: relative;
    }
    .theme-box {
      width: 100%;
      height: 62px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      font-weight: 500;
      border: 2px solid transparent;
      position: relative;
    }
    .theme-swatch-wrap.selected .theme-box {
      outline: 2.5px solid #D99B93;
      outline-offset: 2px;
    }
    .pro-pill {
      position: absolute;
      top: -7px;
      background: var(--pro-bg);
      color: var(--pro-text);
      font-size: 10px;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 999px;
    }
    .check-badge {
      position: absolute;
      top: 4px;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      background: #D99B93;
      color: #3E1411;
      font-size: 11px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
    }
    /* Switch Row */
    .switch-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 0;
      cursor: pointer;
    }
    .m3-switch {
      width: 52px;
      height: 30px;
      border-radius: 999px;
      background: #332E2E;
      border: 1.5px solid #8C8481;
      position: relative;
      transition: 0.2s;
    }
    .m3-switch.on {
      background: #D99B93;
      border-color: #D99B93;
    }
    .m3-thumb {
      width: 22px;
      height: 22px;
      border-radius: 50%;
      background: #938B88;
      position: absolute;
      top: 2.5px;
      left: 4px;
      transition: 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      color: transparent;
    }
    .m3-switch.on .m3-thumb {
      left: 24px;
      background: #5C1E1B;
      color: #D99B93;
    }
    /* Quick Action 4 Tiles (Screenshot 6) */
    .quick-tiles {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 10px;
      margin: 14px 0;
    }
    .quick-tile {
      background: #282424;
      border-radius: 16px;
      height: 80px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      font-size: 13px;
      color: #D6CECB;
      cursor: pointer;
    }
    .menu-row {
      display: flex;
      align-items: center;
      gap: 18px;
      padding: 13px 4px;
      cursor: pointer;
      font-size: 16px;
    }
    /* Pro Banner Card (Screenshot 5 & 7) */
    .pro-banner {
      background: #261716;
      border-radius: 20px;
      padding: 16px 18px;
      display: flex;
      align-items: center;
      gap: 16px;
      cursor: pointer;
      margin-bottom: 18px;
    }
    .toast {
      position: absolute;
      bottom: 84px;
      left: 50%;
      transform: translateX(-50%);
      background: #322928;
      color: #ECE6E4;
      padding: 10px 18px;
      border-radius: 999px;
      font-size: 13.5px;
      z-index: 90;
      display: none;
      box-shadow: 0 4px 16px rgba(0,0,0,0.4);
      white-space: nowrap;
    }
    .toast.show { display: block; }
    /* Cheat Sheet Card Styles matching Screenshot 4 */
    .cs-badge {
      display: inline-block;
      background: #F6EAD4;
      color: #B45F1E;
      font-size: 9px;
      font-weight: 700;
      padding: 3px 7px;
      border-radius: 4px;
      margin-bottom: 4px;
    }
    .cs-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px;
      margin-top: 8px;
    }
    .cs-box {
      background: #fff;
      border: 1px solid #D2D8E2;
      border-radius: 7px;
      padding: 8px;
      font-size: 9.5px;
      line-height: 1.35;
    }
    .cs-box h4 {
      font-size: 10px;
      color: #161C2A;
      margin-bottom: 5px;
      display: flex;
      justify-content: space-between;
    }
    .cs-formula {
      background: #EFF4FC;
      border: 1px solid #A0BCEB;
      border-radius: 5px;
      padding: 5px;
      margin: 4px 0;
      font-weight: 600;
      color: #142034;
    }
    .cs-mistake {
      background: #FDF2F2;
      border-radius: 4px;
      padding: 4px 5px;
      margin-bottom: 4px;
    }
    .orion-app-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: radial-gradient(circle at 50% 50%, #2B1A18 60%, #1A1211 100%);
      border: 1px solid rgba(217,155,147,0.35);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      flex-shrink: 0;
      box-shadow: 0 2px 8px rgba(0,0,0,0.4);
    }
    .launcher-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(165deg, #171214 0%, #0E0B0C 60%, #1F1413 100%);
      z-index: 85;
      display: none;
      flex-direction: column;
      align-items: center;
      justify-content: space-between;
      padding: 48px 24px 32px;
    }
    .launcher-overlay.open { display: flex; }
    .launcher-app-tile {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      transition: transform 0.15s;
    }
    .launcher-app-tile:active { transform: scale(0.93); }
    .launcher-icon-big {
      width: 84px;
      height: 84px;
      border-radius: 22px;
      background: radial-gradient(circle at 50% 50%, #2B1A18 62%, #1A1211 100%);
      border: 2px solid rgba(217,155,147,0.45);
      box-shadow: 0 10px 28px rgba(0,0,0,0.65);
      display: flex;
      align-items: center;
      justify-content: center;
    }
  </style>
</head>
<body>
<div class="app-shell">
  <!-- ANDROID HOME SCREEN LAUNCHER OVERLAY WITH ORION PDF ICON -->
  <div class="launcher-overlay open" id="launcherOverlay">
    <div style="text-align:center; color:#B5AAA7; font-size:13px">Android Phone Launcher • Tap Icon Below to Open App</div>
    <div class="launcher-app-tile" onclick="document.getElementById('launcherOverlay').classList.remove('open')">
      <div class="launcher-icon-big" style="background:linear-gradient(135deg,#2E1614 0%,#160C0B 100%)">
        <svg width="64" height="64" viewBox="0 0 108 108">
          <path fill="#9E473E" d="M34,29 L62,29 C65.3,29 68,31.7 68,35 L68,73 C68,76.3 65.3,79 62,79 L34,79 C30.7,79 28,76.3 28,73 L28,35 C28,31.7 30.7,29 34,29 Z"/>
          <path fill="#F5EFEA" d="M40,25 L64,25 L78,39 L78,75 C78,78.3 75.3,81 72,81 L40,81 C36.7,81 34,78.3 34,75 L34,31 C34,27.7 36.7,25 40,25 Z"/>
          <path fill="#D99B93" d="M64,25 L64,35 C64,37.2 65.8,39 68,39 L78,39 Z"/>
          <path fill="#B83B31" d="M42,25 L50,25 L50,45 L46,41.5 L42,45 Z"/>
          <path fill="#2B1D1B" d="M42,51 L70,51 L70,55 L42,55 Z M42,59 L64,59 L64,63 L42,63 Z"/>
          <path fill="#E3B34C" d="M42,67 L58,67 L58,71 L42,71 Z"/>
          <path fill="#B83B31" d="M68,61 L70.2,66.8 L76,69 L70.2,71.2 L68,77 L65.8,71.2 L60,69 L65.8,66.8 Z"/>
        </svg>
      </div>
      <div style="font-size:16px; font-weight:600; color:#ECE6E4">Orion PDF Reader</div>
      <div style="font-size:12.5px; color:#3E1411; background:#D99B93; font-weight:600; padding:6px 16px; border-radius:999px; margin-top:4px">Tap Here to Launch Orion PDF Reader</div>
    </div>
    <div style="background:#221A19; border:1px solid #3D2B29; border-radius:14px; padding:12px 16px; width:100%; font-size:12px; color:#D8CECB; line-height:1.45">
      <strong style="color:#D99B93">How to Download APK to Your Mobile:</strong><br/>
      In Google AI Studio, click the <strong>Settings (⚙️) / Export</strong> button in the <strong>top-right corner</strong> of the page and choose <strong>Download APK</strong> (or <strong>Export ZIP</strong>) to install directly on your Android phone.
    </div>
  </div>

  <!-- LIBRARY TOP BAR -->
  <div class="top-bar">
    <button class="icon-btn" id="folderBackBtn" style="display:none" onclick="exitFolder()">←</button>
    <div class="orion-app-icon" style="background:linear-gradient(135deg,#2E1614 0%,#160C0B 100%)" onclick="document.getElementById('launcherOverlay').classList.add('open')" title="Orion PDF Reader Icon (Tap to view Home Launcher Icon)">
      <svg width="26" height="26" viewBox="0 0 108 108">
        <path fill="#9E473E" d="M34,29 L62,29 C65.3,29 68,31.7 68,35 L68,73 C68,76.3 65.3,79 62,79 L34,79 C30.7,79 28,76.3 28,73 L28,35 C28,31.7 30.7,29 34,29 Z"/>
        <path fill="#F5EFEA" d="M40,25 L64,25 L78,39 L78,75 C78,78.3 75.3,81 72,81 L40,81 C36.7,81 34,78.3 34,75 L34,31 C34,27.7 36.7,25 40,25 Z"/>
        <path fill="#D99B93" d="M64,25 L64,35 C64,37.2 65.8,39 68,39 L78,39 Z"/>
        <path fill="#B83B31" d="M42,25 L50,25 L50,45 L46,41.5 L42,45 Z"/>
        <path fill="#2B1D1B" d="M42,51 L70,51 L70,55 L42,55 Z M42,59 L64,59 L64,63 L42,63 Z"/>
        <path fill="#E3B34C" d="M42,67 L58,67 L58,71 L42,71 Z"/>
        <path fill="#B83B31" d="M68,61 L70.2,66.8 L76,69 L70.2,71.2 L68,77 L65.8,71.2 L60,69 L65.8,66.8 Z"/>
      </svg>
    </div>
    <div class="top-title" id="topBarTitle">Files</div>
    <button class="icon-btn" onclick="toggleSearch()" title="Search PDFs">🔍</button>
    <button class="icon-btn" onclick="openSheet('homeMoreSheet')" title="More">⋮</button>
  </div>
  <div class="search-bar" id="librarySearchBar">
    <input class="search-input" id="librarySearchInput" placeholder="Search PDFs by title, folder, or author…" oninput="renderLibrary()" />
  </div>

  <!-- MAIN LIBRARY CONTENT -->
  <div class="main-content" id="libraryContent"></div>

  <!-- BOTTOM NAVIGATION BAR -->
  <div class="bottom-nav">
    <div class="nav-item active" id="nav-files" onclick="switchTab('Files')">
      <div class="nav-pill">📄</div>
      <span>Files</span>
    </div>
    <div class="nav-item" id="nav-recent" onclick="switchTab('Recent')">
      <div class="nav-pill">🕒</div>
      <span>Recent</span>
    </div>
    <div class="nav-item" id="nav-favorites" onclick="switchTab('Favorites')">
      <div class="nav-pill">☆</div>
      <span>Favorites</span>
    </div>
    <div class="nav-item" id="nav-folders" onclick="switchTab('Folders')">
      <div class="nav-pill">📁</div>
      <span>Folders</span>
    </div>
  </div>

  <!-- PDF READER FULLSCREEN OVERLAY (Screenshot 4) -->
  <div class="reader-screen" id="readerScreen">
    <div class="top-bar" style="background:#141111; color:#ECE6E4">
      <button class="icon-btn" style="color:#ECE6E4" onclick="closeReader()">←</button>
      <div class="top-title" id="readerDocTitle">Class_10 Maths Cheat-S...</div>
      <button class="icon-btn" style="color:#ECE6E4" onclick="toggleInPdfSearch()">🔍</button>
      <button class="icon-btn" style="color:#ECE6E4" onclick="openSheet('docActionSheet')">⋮</button>
    </div>
    <div class="search-bar" id="inPdfSearchBar" style="background:#1E1A1A; padding-top:8px">
      <input class="search-input" id="inPdfSearchInput" placeholder="Search inside PDF (e.g. Linear, Ratio, Shortcut)…" oninput="runInPdfSearch()" />
      <span id="inPdfMatchBadge" style="font-size:12px; color:#D99B93">0/0</span>
    </div>

    <div class="reader-viewport" id="readerViewport" onscroll="updateCurrentPageOnScroll()">
      <div id="readerPagesContainer"></div>
    </div>

    <!-- Floating Left Sidebar Pill with Integrated Annotation Tools -->
    <div class="floating-sidebar">
      <div class="sidebar-pill" id="annotationDock">
        <button class="side-btn" id="tool-pen" onclick="selectTool('pen')" title="Freehand Pen">✏️</button>
        <button class="side-btn" id="tool-marker" onclick="selectTool('marker')" title="Marker">🖌️</button>
        <button class="side-btn" id="tool-highlight" onclick="selectTool('highlight')" title="Highlighter">🖍️</button>
        <button class="side-btn" id="tool-underline" onclick="selectTool('underline')" title="Underline"><u>U</u></button>
        <button class="side-btn" id="tool-note" onclick="selectTool('note')" title="Sticky Note Callout">📌</button>
        <button class="side-btn" id="tool-eraser" onclick="selectTool('eraser')" title="Eraser">🧹</button>
        <button class="side-btn" onclick="cycleColor()" title="Change Ink Color">
          <div class="color-dot" id="activeColorDot" style="background:#E53935"></div>
        </button>
        <button class="side-btn" onclick="undoStroke()" title="Undo">↶</button>
      </div>
      <div class="sidebar-pill">
        <button class="side-btn active" id="btnToggleAnnotate" onclick="toggleAnnotationDock()" title="Toggle Annotation Dock">✒️</button>
        <button class="side-btn" id="btnLockScroll" onclick="toggleLockScroll()" title="Lock Scroll">🔓</button>
        <button class="side-btn" onclick="rotatePages()" title="Rotate 90°">↻</button>
        <button class="side-btn" onclick="resetZoomAndRotation()" title="Orientation Reset">🔄</button>
      </div>
    </div>

    <!-- Bottom Reader Bar -->
    <div class="reader-bottom-bar">
      <div class="nav-item" onclick="openSheet('contentsSheet')">
        <span style="font-size:18px">☰</span>
        <span>Contents</span>
      </div>
      <div class="nav-item" id="btnReflow" onclick="toggleReflow()">
        <span style="font-size:18px">≡</span>
        <span>Reflow</span>
      </div>
      <div class="page-counter-pill" id="pageCounterPill" onclick="promptJumpPage()">3 / 14</div>
      <div class="nav-item" id="btnBookmark" onclick="toggleBookmark()">
        <span style="font-size:18px" id="bookmarkIcon">🔖</span>
        <span>Bookmark</span>
      </div>
      <div class="nav-item" onclick="openSheet('readingSettingsSheet')">
        <span style="font-size:18px">🎛️</span>
        <span>Display</span>
      </div>
    </div>
  </div>

  <!-- READING SETTINGS BOTTOM SHEET (Screenshot 3) -->
  <div class="sheet-backdrop" id="readingSettingsSheet" onclick="if(event.target===this)closeSheet('readingSettingsSheet')">
    <div class="bottom-sheet">
      <div class="sheet-handle"></div>
      <h3 style="font-size:22px; font-weight:500; margin-bottom:18px">Reading settings</h3>
      <div style="display:flex; align-items:center; gap:14px; margin-bottom:14px">
        <span>🔅</span>
        <input type="range" min="30" max="100" value="75" style="flex:1; accent-color:#D99B93" oninput="setBrightness(this.value)" />
        <span>🔆</span>
      </div>
      <div class="switch-row" onclick="toggleSwitch('sysBrightness')">
        <span>Use system brightness</span>
        <div class="m3-switch on" id="sw-sysBrightness"><div class="m3-thumb">✓</div></div>
      </div>

      <div class="theme-grid" id="readingThemeGrid"></div>

      <div class="switch-row" onclick="toggleSwitch('verticalScroll')">
        <span>Vertical scroll</span>
        <div class="m3-switch on" id="sw-verticalScroll"><div class="m3-thumb">✓</div></div>
      </div>
      <div class="switch-row" onclick="toggleSwitch('swipeByPage')">
        <span>Swipe by page</span>
        <div class="m3-switch" id="sw-swipeByPage"><div class="m3-thumb">✓</div></div>
      </div>
      <div class="switch-row" onclick="toggleSwitch('showScrollBar')">
        <span>Show scroll bar</span>
        <div class="m3-switch on" id="sw-showScrollBar"><div class="m3-thumb">✓</div></div>
      </div>
    </div>
  </div>

  <!-- DOCUMENT ACTION BOTTOM SHEET (Screenshot 6) -->
  <div class="sheet-backdrop" id="docActionSheet" onclick="if(event.target===this)closeSheet('docActionSheet')">
    <div class="bottom-sheet">
      <div class="sheet-handle"></div>
      <h3 id="actionSheetDocTitle" style="font-size:18px; font-weight:500; margin-bottom:6px">Class_10 Maths Cheat-Sheet Share</h3>
      <div class="quick-tiles">
        <div class="quick-tile" onclick="closeSheet('docActionSheet'); showToast('Sharing PDF via Android ShareSheet…')"><span>🔗</span><span>Share</span></div>
        <div class="quick-tile" onclick="closeSheet('docActionSheet'); window.open('/sample.pdf','_blank')"><span>↗️</span><span>Open with</span></div>
        <div class="quick-tile" onclick="closeSheet('docActionSheet'); window.print()"><span>🖨️</span><span>Print</span></div>
        <div class="quick-tile" onclick="closeSheet('docActionSheet'); showFileInfo()"><span>ℹ️</span><span>File info</span></div>
      </div>
      <div class="menu-row" onclick="closeSheet('docActionSheet'); startReadAloud()">
        <span>🎧</span><span style="flex:1">Read aloud</span><span class="pro-pill" style="position:static">Pro</span>
      </div>
      <div class="menu-row" onclick="closeSheet('docActionSheet'); startAutoScroll()">
        <span>⏬</span><span style="flex:1">Auto-scroll</span><span class="pro-pill" style="position:static">Pro</span>
      </div>
      <hr style="border:none; border-top:1px solid #312C2C; margin:6px 0" />
      <div class="menu-row" onclick="closeSheet('docActionSheet'); openSheet('contentsSheet')">
        <span>🔖</span><span>Bookmarks</span>
      </div>
      <div class="menu-row" onclick="closeSheet('docActionSheet'); openSheet('contentsSheet')">
        <span>❝</span><span>Quotes</span>
      </div>
      <hr style="border:none; border-top:1px solid #312C2C; margin:6px 0" />
      <div class="menu-row" onclick="closeSheet('docActionSheet'); showToast('Pinned reading shortcut to notifications')">
        <span>🔔</span><span>Pin notification</span>
      </div>
      <div class="menu-row" onclick="closeSheet('docActionSheet'); showInFolderFromMenu()">
        <span>📁</span><span>Show in folder</span>
      </div>
      <hr style="border:none; border-top:1px solid #312C2C; margin:6px 0" />
      <div class="menu-row" onclick="closeSheet('docActionSheet'); deleteCurrentDoc()">
        <span>🗑️</span><span>Delete</span>
      </div>
    </div>
  </div>

  <!-- HOME MORE MENU SHEET (Screenshot 5) -->
  <div class="sheet-backdrop" id="homeMoreSheet" onclick="if(event.target===this)closeSheet('homeMoreSheet')">
    <div class="bottom-sheet">
      <div class="sheet-handle"></div>
      <div class="pro-banner" onclick="closeSheet('homeMoreSheet'); openSheet('proSheet')">
        <div style="font-size:34px">📚</div>
        <div style="flex:1">
          <div style="font-family:Georgia,serif; font-size:20px; font-weight:bold">Pro Version</div>
          <div style="font-size:13px; color:#B5A8A4; margin-top:3px">No ads, unlimited search, quotes and bookmarks</div>
        </div>
        <span>›</span>
      </div>

      <div style="font-size:14px; color:#A79E9B; margin-bottom:8px">Theme</div>
      <div class="segmented-pill" style="margin-bottom:16px">
        <button class="seg-btn active" id="appTheme-system" onclick="setAppTheme('system')">◐ System</button>
        <button class="seg-btn" id="appTheme-light" onclick="setAppTheme('light')">☀️ Light</button>
        <button class="seg-btn" id="appTheme-dark" onclick="setAppTheme('dark')">🌙 Dark</button>
      </div>

      <label class="menu-row" style="cursor:pointer">
        <span>📄</span>
        <div style="flex:1">
          <div>Open a file from device</div>
          <div style="font-size:12.5px; color:#9C9390">Any PDF, via the system file picker</div>
        </div>
        <input type="file" accept="application/pdf" style="display:none" onchange="handleFileSelect(event)" />
      </label>
      <div class="menu-row" onclick="closeSheet('homeMoreSheet'); showToast('Storage rescanned • All PDFs indexed')">
        <span>🔄</span><span>Rescan storage</span>
      </div>
      <div class="menu-row" onclick="closeSheet('homeMoreSheet'); openSheet('apkSideloadSheet')">
        <span>📲</span><span>Download APK / Sideload Guide</span>
      </div>
      <hr style="border:none; border-top:1px solid #312C2C; margin:6px 0" />
      <div class="menu-row" onclick="closeSheet('homeMoreSheet'); showToast('Thank you for your 5 ★ rating!')">
        <span>⭐</span><span>Leave a rating</span>
      </div>
      <div class="menu-row" onclick="closeSheet('homeMoreSheet'); showToast('All Pro features are unlocked')">
        <span>💬</span><span>Feedback</span>
      </div>
      <div style="text-align:center; font-size:12px; color:#8C827F; margin-top:12px">Version 1.18.2 • Pro Unlocked</div>
    </div>
  </div>

  <!-- PRO VERSION SHOWCASE SHEET (Screenshot 7) -->
  <div class="sheet-backdrop" id="proSheet" onclick="if(event.target===this)closeSheet('proSheet')">
    <div class="bottom-sheet" style="max-height:95vh">
      <div class="sheet-handle"></div>
      <div style="text-align:center; padding:12px 0 20px; background:#221413; border-radius:20px; margin-bottom:16px">
        <div style="font-size:48px">📑</div>
        <h2 style="font-family:Georgia,serif; font-size:28px; margin-top:8px">Pro Version</h2>
        <p style="font-size:13.5px; color:#A79A96; margin-top:4px">Lifetime access to all premium features<br/>All Pro tools unlocked by default</p>
      </div>
      <div class="menu-row"><span>🚫</span><div><div>Remove ads</div><div style="font-size:12px; color:#9C9390">Read PDFs without banner ads</div></div></div>
      <div class="menu-row"><span>📋</span><div><div>Text selection tools</div><div style="font-size:12px; color:#9C9390">Share, translate, and search selected text</div></div></div>
      <div class="menu-row"><span>🔍</span><div><div>Unlimited PDF search</div><div style="font-size:12px; color:#9C9390">Search inside any PDF without limits</div></div></div>
      <div class="menu-row"><span>🎧</span><div><div>Read aloud & Auto-scroll</div><div style="font-size:12px; color:#9C9390">Listen to your PDF or scroll hands-free</div></div></div>
      <div class="menu-row"><span>🎨</span><div><div>8 Eye-Comfort Reading Themes</div><div style="font-size:12px; color:#9C9390">Light, Sepia, Dark, Mist, Sage, Sand, Dusk & Candle</div></div></div>
      <button onclick="closeSheet('proSheet'); showToast('All Pro features active!')" style="width:100%; margin-top:14px; padding:15px; border-radius:999px; border:none; background:#D99B93; color:#3E1411; font-size:16px; font-weight:600; cursor:pointer">All Pro Features Unlocked ✓</button>
    </div>
  </div>

  <!-- CONTENTS / BOOKMARKS / QUOTES SHEET -->
  <div class="sheet-backdrop" id="contentsSheet" onclick="if(event.target===this)closeSheet('contentsSheet')">
    <div class="bottom-sheet">
      <div class="sheet-handle"></div>
      <h3 style="font-size:20px; margin-bottom:12px">Chapters, Bookmarks & Quotes</h3>
      <div id="contentsListContainer"></div>
    </div>
  </div>

  <!-- APK SIDELOAD & ANDROID SOURCE BUNDLE SHEET -->
  <div class="sheet-backdrop" id="apkSideloadSheet" onclick="if(event.target===this)closeSheet('apkSideloadSheet')">
    <div class="bottom-sheet">
      <div class="sheet-handle"></div>
      <h3 style="font-size:20px; margin-bottom:10px">Sideload APK & Android Kotlin Project</h3>
      <p style="font-size:13.5px; color:#B5A8A4; line-height:1.5; margin-bottom:14px">
        This project contains the complete native Android Kotlin &amp; Jetpack Compose source code (<code>app/src/main/java/com/example/...</code>) configured with Room SQLite, <code>PdfRenderer</code> LRU caching, and release/debug signing.
      </p>
      <div style="background:#282424; padding:12px; border-radius:12px; font-size:13px; line-height:1.55; margin-bottom:14px">
        <strong>How to install on your phone:</strong><br/>
        1. Click <strong>Export / Download ZIP</strong> in the top-right AI Studio menu.<br/>
        2. Run <code>gradle assembleDebug</code> in Android Studio or CLI to generate <code>app-debug.apk</code>, or use the direct sample PDF download below.
      </div>
      <button onclick="window.open('/sample.pdf','_blank')" style="width:100%; padding:14px; border-radius:999px; border:none; background:#D99B93; color:#3E1411; font-weight:600; cursor:pointer">Download Class 10 Maths Cheat-Sheet PDF</button>
    </div>
  </div>

  <div class="toast" id="toastBox"></div>
</div>

<script>
  const documents = [
    { id: 1, title: "Class_10 Maths Cheat-Sheet ...", fullTitle: "Class_10 Maths Cheat-Sheet Complete Formula Vault", date: "Mar 15, 2026", size: "1.8 MB", folder: ".DPP Class 10", volume: "Internal storage", author: "Kaushal Maths Academy", pageCount: 14, currentPage: 3, lastOpened: "4 min. ago", progress: 21, favorite: true },
    { id: 2, title: "NCERT Ganit Mathematics X", fullTitle: "NCERT Ganit Mathematics Student Support Material", date: "Jan 20, 2026", size: "4.2 MB", folder: ".DPP Class 10", volume: "Internal storage", author: "Kendriya Vidyalaya", pageCount: 12, currentPage: 6, lastOpened: "28 min. ago", progress: 50, favorite: true },
    { id: 3, title: "REET Syllabus L2 2024-25", fullTitle: "REET Syllabus L2 2024-25", date: "Feb 9, 2025", size: "2.6 MB", folder: "Download", volume: "Internal storage", author: "BSER Rajasthan", pageCount: 8, currentPage: 1, lastOpened: "", progress: 0, favorite: false },
    { id: 4, title: "SI RPSC Card", fullTitle: "SI RPSC Card", date: "Apr 2, 2026", size: "0.96 MB", folder: "Download", volume: "Internal storage", author: "RPSC Ajmer", pageCount: 4, currentPage: 1, lastOpened: "", progress: 0, favorite: false },
    { id: 5, title: "SI RPSC Card", fullTitle: "SI RPSC Card (Print Verified)", date: "Apr 4, 2026", size: "0.92 MB", folder: "Print", volume: "Internal storage", author: "RPSC Ajmer", pageCount: 4, currentPage: 1, lastOpened: "", progress: 0, favorite: false },
    { id: 6, title: "(6) Spoken Engl... uccess Darpan", fullTitle: "(6) Spoken English Success Darpan", date: "Aug 9, 2024", size: "3.3 MB", folder: "Study Notes", volume: "Internal storage", author: "Success Darpan", pageCount: 10, currentPage: 2, lastOpened: "3 hr. ago", progress: 20, favorite: true },
    { id: 7, title: "(S@roj)-Book of... History (Hindi)", fullTitle: "(S@roj)-Book of History (Hindi)", date: "Oct 5, 2025", size: "5.1 MB", folder: "Telegram Videos", volume: "Internal storage", author: "Saroj Civil Services", pageCount: 10, currentPage: 1, lastOpened: "", progress: 0, favorite: false }
  ];

  const foldersList = [
    { name: ".Alarms", count: 1, volume: "B437-1C18" },
    { name: ".Chats", count: 4, volume: "B437-1C18" },
    { name: ".DPP Class 10", count: 3, volume: "Internal storage" },
    { name: ".Kaushal Document", count: 21, volume: "Internal storage" },
    { name: ".Lokesh Documents", count: 7, volume: "Internal storage" },
    { name: ".Mummy Document", count: 32, volume: "Internal storage" },
    { name: ".other", count: 7, volume: "B437-1C18" },
    { name: ".Payal Document", count: 13, volume: "Internal storage" }
  ];

  const readingThemes = [
    { id: "light", name: "Light", bg: "#D0CECC", text: "#171515", pro: false, filter: "none" },
    { id: "sepia", name: "Sepia", bg: "#CEC8B6", text: "#1E1A14", pro: false, filter: "sepia(0.45) brightness(0.95)" },
    { id: "dark", name: "Dark", bg: "#050505", text: "#949290", pro: false, filter: "invert(0.9) hue-rotate(180deg)" },
    { id: "mist", name: "Mist", bg: "#BAC1C7", text: "#1D2226", pro: true, filter: "saturate(0.75) brightness(0.92)" },
    { id: "sage", name: "Sage", bg: "#B3BDB5", text: "#1B231D", pro: true, filter: "sepia(0.25) hue-rotate(65deg) brightness(0.92)" },
    { id: "sand", name: "Sand", bg: "#C8BEAF", text: "#282018", pro: true, filter: "sepia(0.35) brightness(0.93)" },
    { id: "dusk", name: "Dusk", bg: "#2C3137", text: "#C5CDD4", pro: true, filter: "invert(0.84) hue-rotate(180deg) brightness(0.95)" },
    { id: "candle", name: "Candle", bg: "#090707", text: "#C28246", pro: true, filter: "invert(0.92) sepia(0.6) saturate(1.8) hue-rotate(330deg)" }
  ];

  let activeTab = "Files";
  let foldersSegment = "folders";
  let selectedFolder = null;
  let isGrid = false;
  let sortAsc = true;
  let activeDoc = documents[0];
  let selectedReadingTheme = "light";
  let activeTool = null;
  let inkColors = ["#E53935", "#FDD835", "#1E88E5", "#43A047", "#FB8C00"];
  let colorIdx = 0;
  let pageRotation = 0;
  let isScrollLocked = false;
  let isReflow = false;
  let autoScrollTimer = null;
  let bookmarks = [{ page: 3, title: "Ch 3: Pair of Linear Equations Ratio Vault" }];
  let pageStrokes = {};

  function switchTab(tab) {
    activeTab = tab;
    selectedFolder = null;
    document.getElementById("folderBackBtn").style.display = "none";
    document.getElementById("topBarTitle").textContent = tab;
    ["files", "recent", "favorites", "folders"].forEach(id => {
      document.getElementById("nav-" + id).classList.toggle("active", id === tab.toLowerCase());
    });
    renderLibrary();
  }

  function exitFolder() {
    selectedFolder = null;
    document.getElementById("folderBackBtn").style.display = "none";
    document.getElementById("topBarTitle").textContent = activeTab;
    renderLibrary();
  }

  function openFolder(name) {
    selectedFolder = name;
    document.getElementById("folderBackBtn").style.display = "inline-flex";
    document.getElementById("topBarTitle").textContent = name;
    renderLibrary();
  }

  function toggleSearch() {
    const bar = document.getElementById("librarySearchBar");
    bar.classList.toggle("open");
    if (bar.classList.contains("open")) document.getElementById("librarySearchInput").focus();
  }

  function renderLibrary() {
    const container = document.getElementById("libraryContent");
    const q = (document.getElementById("librarySearchInput").value || "").toLowerCase();

    if (activeTab === "Folders" && !selectedFolder) {
      const items = foldersSegment === "folders"
        ? foldersList
        : [...new Set(documents.map(d => d.author))].map(a => ({ name: a, count: documents.filter(d => d.author === a).length, volume: "Internal storage" }));

      container.innerHTML = \`
        <div class="segmented-wrap">
          <div class="segmented-pill">
            <button class="seg-btn \${foldersSegment === 'folders' ? 'active' : ''}" onclick="foldersSegment='folders'; renderLibrary()">📁 Folders</button>
            <button class="seg-btn \${foldersSegment === 'authors' ? 'active' : ''}" onclick="foldersSegment='authors'; renderLibrary()">👤 Authors</button>
          </div>
          <button class="icon-btn" onclick="showToast('Filtered by all storage volumes')">🎛️</button>
        </div>
        \${items.map(f => \`
          <div class="folder-row" onclick="openFolder('\${f.name}')">
            <div class="folder-icon-box">\${foldersSegment === 'folders' ? '📁' : '👤'}</div>
            <div style="flex:1">
              <div style="font-size:17px">\${f.name}</div>
              <div style="font-size:13.5px; color:var(--muted); margin-top:2px">\${f.count} \${f.count === 1 ? 'PDF' : 'PDFs'} · \${f.volume}</div>
            </div>
            <span style="color:var(--muted); font-size:18px">›</span>
          </div>
        \`).join("")}
      \`;
      return;
    }

    let list = documents.slice();
    if (selectedFolder) {
      list = list.filter(d => d.folder === selectedFolder || d.author === selectedFolder);
    } else if (activeTab === "Recent") {
      list = list.filter(d => d.lastOpened);
    } else if (activeTab === "Favorites") {
      list = list.filter(d => d.favorite);
    }
    if (q) {
      list = list.filter(d => d.title.toLowerCase().includes(q) || d.folder.toLowerCase().includes(q));
    }
    list.sort((a, b) => sortAsc ? a.title.localeCompare(b.title) : b.title.localeCompare(a.title));

    let html = "";
    if (activeTab === "Files" && !selectedFolder && !q) {
      const recents = documents.filter(d => d.lastOpened);
      html += \`
        <div class="section-label">Continue reading</div>
        <div class="carousel">
          \${recents.map(d => \`
            <div class="continue-card" onclick="openReader(\${d.id})">
              <div class="pdf-thumb">
                <div class="pdf-thumb-header">CBSE X</div>
                <div>Ch 3: Linear Equations Formula Vault</div>
                <div style="color:#1D4EA6; font-weight:bold">p.\${d.currentPage}/\${d.pageCount}</div>
              </div>
              <div class="continue-info">
                <div class="continue-title">\${d.title}</div>
                <div class="continue-meta">Page \${d.currentPage} of \${d.pageCount} · \${d.lastOpened}</div>
                <div class="progress-row">
                  <div class="progress-track"><div class="progress-fill" style="width:\${d.progress}%"></div></div>
                  <span class="progress-pct">\${d.progress}%</span>
                </div>
              </div>
            </div>
          \`).join("")}
        </div>
      \`;
    }

    html += \`
      <div class="toolbar-row">
        <button class="sort-btn" onclick="sortAsc=!sortAsc; renderLibrary()">
          <span>☰</span><span>Name</span><span>\${sortAsc ? '↑' : '↓'}</span>
        </button>
        <div style="flex:1"></div>
        <button class="icon-btn" onclick="isGrid=!isGrid; renderLibrary()">⊞</button>
        <button class="icon-btn" onclick="showToast('Showing Internal Storage & SD Card PDFs')">🎛️</button>
      </div>
    \`;

    if (isGrid) {
      html += \`<div class="pdf-grid">\${list.map(d => \`
        <div class="pdf-grid-card" onclick="openReader(\${d.id})">
          <div class="pdf-thumb" style="width:100%; height:110px; margin-bottom:8px">
            <div class="pdf-thumb-header">\${d.folder}</div>
            <div style="font-size:8px; font-weight:600; margin-top:4px">\${d.fullTitle}</div>
          </div>
          <div class="pdf-row-title" style="font-size:14px">\${d.title}</div>
          <div class="pdf-row-sub" style="font-size:11.5px">\${d.size} · \${d.folder}</div>
        </div>
      \`).join("")}</div>\`;
    } else {
      html += list.map(d => \`
        <div class="pdf-row" onclick="openReader(\${d.id})">
          <div class="pdf-thumb">
            <div class="pdf-thumb-header">PDF</div>
            <div>\${d.title.slice(0, 18)}</div>
            <div style="color:#666">\${d.pageCount}p</div>
          </div>
          <div class="pdf-row-info">
            <div class="pdf-row-title">\${d.title}</div>
            <div class="pdf-row-sub">\${d.date} · \${d.size} · \${d.folder}</div>
          </div>
          <button class="icon-btn" onclick="event.stopPropagation(); activeDoc=documents.find(x=>x.id===\${d.id}); document.getElementById('actionSheetDocTitle').textContent=activeDoc.fullTitle; openSheet('docActionSheet')">⋮</button>
        </div>
      \`).join("");
    }

    container.innerHTML = html;
  }

  function openReader(docId) {
    activeDoc = documents.find(d => d.id === docId) || documents[0];
    document.getElementById("readerDocTitle").textContent = activeDoc.title;
    document.getElementById("actionSheetDocTitle").textContent = activeDoc.fullTitle;
    document.getElementById("readerScreen").classList.add("open");
    renderReaderPages();
    setTimeout(() => {
      const targetEl = document.getElementById("pdf-page-" + activeDoc.currentPage);
      if (targetEl) targetEl.scrollIntoView({ behavior: "instant" });
    }, 30);
  }

  function closeReader() {
    stopAutoScroll();
    window.speechSynthesis?.cancel();
    document.getElementById("readerScreen").classList.remove("open");
  }

  function renderReaderPages() {
    const container = document.getElementById("readerPagesContainer");
    const chapters = [
      "Chapter 1: Real Numbers & Euclid Lemma",
      "Chapter 2: Polynomials & Zeroes Relationship",
      "Chapter 3: Pair of Linear Equations in Two Variables",
      "Chapter 4: Quadratic Equations & Nature of Roots",
      "Chapter 5: Arithmetic Progressions (nth Term & Sum)",
      "Chapter 6: Triangles & Basic Proportionality Theorem",
      "Chapter 7: Coordinate Geometry (Distance & Section Formula)",
      "Chapter 8: Introduction to Trigonometry Identities",
      "Chapter 9: Heights and Distances Applications",
      "Chapter 10: Circles & Tangent Properties",
      "Chapter 11: Areas Related to Circles (Sectors & Segments)",
      "Chapter 12: Surface Areas and Volumes Vault",
      "Chapter 13: Statistics (Direct, Assumed Mean & Mode)",
      "Chapter 14: Probability Complete Revision Sheet"
    ];

    let html = "";
    for (let p = 1; p <= activeDoc.pageCount; p++) {
      const chTitle = chapters[(p - 1) % chapters.length];
      if (isReflow) {
        html += \`
          <div class="pdf-page-sheet" id="pdf-page-\${p}" style="font-size:16px; line-height:1.65; padding:22px">
            <div style="color:#1D4EA6; font-weight:700; font-size:13px">REFLOW MODE • PAGE \${p} OF \${activeDoc.pageCount}</div>
            <h3 style="margin:6px 0 10px; font-size:19px">\${chTitle}</h3>
            <p><strong>1. Anatomy of Linear Pair:</strong> Standard General Form is a₁x + b₁y + c₁ = 0 and a₂x + b₂y + c₂ = 0. Every solution (x, y) is a point lying on the straight line.</p>
            <p style="margin-top:8px"><strong>2. Master Ratio & Consistency Vault:</strong> a₁/a₂ ≠ b₁/b₂ gives Intersecting Lines (Unique Solution). a₁/a₂ = b₁/b₂ = c₁/c₂ gives Coincident Lines (Infinitely Many Solutions).</p>
            <p style="margin-top:8px"><strong>3. Topper 5-Second MCQ Shortcut:</strong> For interchanged coefficients (49x + 51y = 499 & 51x + 49y = 501), add both equations first to get x + y = 10, then subtract to get x - y = 1!</p>
          </div>
        \`;
      } else {
        html += \`
          <div class="pdf-page-sheet" id="pdf-page-\${p}" style="transform:rotate(\${pageRotation}deg)">
            <canvas class="annotation-canvas \${activeTool ? 'drawing' : ''}" id="canvas-page-\${p}" width="440" height="580" onpointerdown="startDraw(event, \${p})" onpointermove="moveDraw(event, \${p})" onpointerup="endDraw(event, \${p})"></canvas>
            <div style="display:flex; justify-content:space-between; align-items:flex-start">
              <div>
                <span class="cs-badge">CBSE CLASS 10 WEIGHTAGE: 4-5 MARKS</span>
                <h3 style="font-size:13.5px; color:#141A26">\${chTitle}</h3>
                <div style="font-size:8.5px; color:#4B5566; font-weight:600">1-Page High-Yield Formula, Shortcut & Mistake Revision Sheet (Rationalised Syllabus)</div>
              </div>
              <div style="background:#EBF2FC; border:1px solid #8CAFE6; border-radius:6px; padding:4px 7px; text-align:center; font-size:8px; color:#1C4EA8; font-weight:700">
                MICRO-STORE EDITION<br/>Page \${p} of \${activeDoc.pageCount}
              </div>
            </div>
            <div class="cs-grid">
              <div class="cs-box">
                <h4><span>1. ANATOMY OF LINEAR PAIR</span><span style="color:#788291">CORE RULE</span></h4>
                <div class="cs-formula">a₁x + b₁y + c₁ = 0 and a₂x + b₂y + c₂ = 0</div>
                <div>• <strong>Geometrical Meaning:</strong> Every solution (x,y) lies on the line.</div>
                <div>• <strong>Axis Equations:</strong> X-axis is y = 0; Y-axis is x = 0.</div>
                <div>• <strong>Parallel to Axes:</strong> x = k is parallel to Y-axis; y = k is parallel to X-axis.</div>
              </div>
              <div class="cs-box">
                <h4><span>2. MASTER RATIO & CONSISTENCY VAULT</span></h4>
                <div>• <strong>a₁/a₂ ≠ b₁/b₂:</strong> Intersecting → Unique (1) → Consistent</div>
                <div>• <strong>a₁/a₂ = b₁/b₂ = c₁/c₂:</strong> Coincident → Infinite → Dependent</div>
                <div>• <strong>a₁/a₂ = b₁/b₂ ≠ c₁/c₂:</strong> Parallel → No Solution</div>
                <div class="cs-formula">Original No. = 10x + y | Reversed = 10y + x</div>
              </div>
              <div class="cs-box">
                <h4><span>3. TOP 3 BOARD EXAM MISTAKES</span><span style="color:#B92626">AVOID</span></h4>
                <div class="cs-mistake">
                  <div style="color:#B92626">✗ Comparing c₁/c₂ when one c is on LHS & RHS</div>
                  <div style="color:#167D3A">✓ DO: Bring BOTH constants to LHS (= 0) first.</div>
                </div>
                <div class="cs-mistake">
                  <div style="color:#B92626">✗ Writing Two-Digit Number as xy instead of 10x+y</div>
                  <div style="color:#167D3A">✓ DO: A 2-digit number is ALWAYS 10x + y.</div>
                </div>
              </div>
              <div class="cs-box">
                <h4><span>4. TOPPER 5-SECOND MCQ SHORTCUTS</span><span style="color:#1C4EA8">VIP TRICK</span></h4>
                <div class="cs-formula">
                  49x + 51y = 499 &amp; 51x + 49y = 501<br/>
                  Add both → x + y = 10 | Subtract → x - y = 1.<br/>
                  Solve in 5 sec!
                </div>
                <div>• (10x + y) + (10y + x) = 11(x + y) (Divisible by 11)</div>
                <div>• (10x + y) - (10y + x) = 9(x - y) (Divisible by 9)</div>
              </div>
            </div>
            <div class="cs-box" style="margin-top:8px">
              <h4><span>5. MUST-SOLVE BOARD QUESTIONS + STEP-MARKING SECRET</span></h4>
              <div><strong>Q1:</strong> Sum of digits of a 2-digit no. is 9. 9× original = 2× reversed. <strong>Ans:</strong> x=1, y=8 → <strong>18</strong>.</div>
              <div style="margin-top:3px"><strong>Q2:</strong> Ratio of incomes 9:7, expenditures 4:3, each saves ₹2000. <strong>Ans:</strong> ₹18,000 &amp; ₹14,000.</div>
              <div style="background:#FEF8E8; border:1px solid #E1B45F; border-radius:5px; padding:5px; margin-top:5px; color:#874E12; font-weight:600">
                💡 STEP-MARKING SECRET: Writing the 3 vertex coordinates clearly and shading the triangle with X-axis carries 1 full mark out of 3!
              </div>
            </div>
          </div>
        \`;
      }
    }
    container.innerHTML = html;
    updatePageCounter(activeDoc.currentPage);
    setTimeout(redrawAllCanvases, 20);
  }

  function updateCurrentPageOnScroll() {
    const vp = document.getElementById("readerViewport");
    const pageHeight = vp.scrollHeight / activeDoc.pageCount;
    const p = Math.min(activeDoc.pageCount, Math.max(1, Math.round(vp.scrollTop / pageHeight) + 1));
    activeDoc.currentPage = p;
    updatePageCounter(p);
  }

  function updatePageCounter(p) {
    document.getElementById("pageCounterPill").textContent = \`\${p} / \${activeDoc.pageCount}\`;
  }

  function promptJumpPage() {
    const input = prompt(\`Jump to page (1 - \${activeDoc.pageCount}):\`, activeDoc.currentPage);
    const p = parseInt(input, 10);
    if (p >= 1 && p <= activeDoc.pageCount) {
      activeDoc.currentPage = p;
      document.getElementById("pdf-page-" + p)?.scrollIntoView({ behavior: "smooth" });
      updatePageCounter(p);
    }
  }

  // Sidebar Annotation Tools
  let drawingPage = null;
  let currentPath = [];

  function selectTool(tool) {
    activeTool = (activeTool === tool) ? null : tool;
    ["pen", "marker", "highlight", "underline", "note", "eraser"].forEach(t => {
      document.getElementById("tool-" + t)?.classList.toggle("active", activeTool === t);
    });
    document.querySelectorAll(".annotation-canvas").forEach(c => {
      c.classList.toggle("drawing", !!activeTool);
    });
    if (activeTool) showToast(\`Sidebar Tool: \${activeTool.toUpperCase()} active — draw or tap on page\`);
  }

  function cycleColor() {
    colorIdx = (colorIdx + 1) % inkColors.length;
    document.getElementById("activeColorDot").style.background = inkColors[colorIdx];
  }

  function startDraw(e, page) {
    if (!activeTool) return;
    const canvas = document.getElementById("canvas-page-" + page);
    const rect = canvas.getBoundingClientRect();
    const x = (e.clientX - rect.left) * (canvas.width / rect.width);
    const y = (e.clientY - rect.top) * (canvas.height / rect.height);

    if (activeTool === "note") {
      const note = prompt("Enter Sticky Note Callout text:", "Important exam formula!");
      if (note) {
        pageStrokes[page] = pageStrokes[page] || [];
        pageStrokes[page].push({ tool: "note", color: inkColors[colorIdx], x, y, note });
        redrawCanvas(page);
        showToast("Pinned sticky note on Page " + page);
      }
      return;
    }
    if (activeTool === "eraser") {
      pageStrokes[page] = [];
      redrawCanvas(page);
      showToast("Cleared annotations on Page " + page);
      return;
    }
    drawingPage = page;
    currentPath = [{ x, y }];
  }

  function moveDraw(e, page) {
    if (drawingPage !== page || !currentPath.length) return;
    const canvas = document.getElementById("canvas-page-" + page);
    const rect = canvas.getBoundingClientRect();
    const x = (e.clientX - rect.left) * (canvas.width / rect.width);
    const y = (e.clientY - rect.top) * (canvas.height / rect.height);
    currentPath.push({ x, y });
    redrawCanvas(page, currentPath);
  }

  function endDraw(e, page) {
    if (drawingPage !== page) return;
    if (currentPath.length > 1) {
      pageStrokes[page] = pageStrokes[page] || [];
      pageStrokes[page].push({ tool: activeTool, color: inkColors[colorIdx], pts: currentPath.slice() });
    }
    drawingPage = null;
    currentPath = [];
    redrawCanvas(page);
  }

  function undoStroke() {
    const p = activeDoc.currentPage;
    if (pageStrokes[p] && pageStrokes[p].length) {
      pageStrokes[p].pop();
      redrawCanvas(p);
      showToast("Undid last annotation");
    }
  }

  function redrawAllCanvases() {
    Object.keys(pageStrokes).forEach(p => redrawCanvas(Number(p)));
  }

  function redrawCanvas(page, livePts) {
    const canvas = document.getElementById("canvas-page-" + page);
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const list = (pageStrokes[page] || []).slice();
    if (livePts && livePts.length > 1) {
      list.push({ tool: activeTool, color: inkColors[colorIdx], pts: livePts });
    }
    list.forEach(item => {
      if (item.tool === "note") {
        ctx.fillStyle = item.color;
        ctx.beginPath();
        ctx.arc(item.x, item.y, 10, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = "#fff";
        ctx.font = "bold 9px sans-serif";
        ctx.fillText("📌", item.x - 5, item.y + 3);
        return;
      }
      ctx.save();
      ctx.strokeStyle = item.color;
      ctx.lineCap = "round";
      ctx.lineJoin = "round";
      ctx.lineWidth = item.tool === "highlight" ? 16 : item.tool === "marker" ? 7 : 3;
      ctx.globalAlpha = item.tool === "highlight" ? 0.36 : item.tool === "marker" ? 0.7 : 0.95;
      ctx.beginPath();
      const first = item.pts[0];
      ctx.moveTo(first.x, first.y);
      if (item.tool === "underline") {
        const last = item.pts[item.pts.length - 1];
        ctx.lineTo(last.x, first.y);
      } else {
        item.pts.forEach(pt => ctx.lineTo(pt.x, pt.y));
      }
      ctx.stroke();
      ctx.restore();
    });
  }

  function toggleAnnotationDock() {
    const dock = document.getElementById("annotationDock");
    const hidden = dock.style.display === "none";
    dock.style.display = hidden ? "flex" : "none";
    document.getElementById("btnToggleAnnotate").classList.toggle("active", hidden);
  }

  function toggleLockScroll() {
    isScrollLocked = !isScrollLocked;
    document.getElementById("readerViewport").style.overflowY = isScrollLocked ? "hidden" : "auto";
    const btn = document.getElementById("btnLockScroll");
    btn.textContent = isScrollLocked ? "🔒" : "🔓";
    btn.classList.toggle("active", isScrollLocked);
    showToast(isScrollLocked ? "Touch scroll locked" : "Touch scroll unlocked");
  }

  function rotatePages() {
    pageRotation = (pageRotation + 90) % 360;
    renderReaderPages();
  }

  function resetZoomAndRotation() {
    pageRotation = 0;
    renderReaderPages();
    showToast("Page orientation reset");
  }

  function toggleReflow() {
    isReflow = !isReflow;
    renderReaderPages();
    showToast(isReflow ? "Text Reflow mode enabled" : "Original PDF layout restored");
  }

  function toggleBookmark() {
    const p = activeDoc.currentPage;
    const idx = bookmarks.findIndex(b => b.page === p);
    if (idx >= 0) {
      bookmarks.splice(idx, 1);
      showToast("Removed bookmark on Page " + p);
    } else {
      bookmarks.push({ page: p, title: \`Page \${p} • \${activeDoc.title}\` });
      showToast("Bookmarked Page " + p);
    }
  }

  function renderReadingThemeGrid() {
    const grid = document.getElementById("readingThemeGrid");
    grid.innerHTML = readingThemes.map(t => {
      const sel = selectedReadingTheme === t.id;
      return \`
        <div class="theme-swatch-wrap \${sel ? 'selected' : ''}" onclick="selectReadingTheme('\${t.id}')">
          <div class="theme-box" style="background:\/*.bg*/\${t.bg}; color:\${t.text}">
            \${sel ? '<div class="check-badge">✓</div>' : (t.pro ? '<div class="pro-pill">Pro</div>' : '')}
            <span>Aa</span>
          </div>
          <span style="font-size:12.5px; color:\${sel ? '#ECE6E4' : '#A29A97'}">\${t.name}</span>
        </div>
      \`;
    }).join("");
  }

  function selectReadingTheme(id) {
    selectedReadingTheme = id;
    const t = readingThemes.find(x => x.id === id) || readingThemes[0];
    const vp = document.getElementById("readerViewport");
    vp.style.filter = t.filter;
    vp.style.background = t.bg;
    renderReadingThemeGrid();
  }

  function setBrightness(val) {
    document.getElementById("readerViewport").style.opacity = (val / 100).toFixed(2);
  }

  function toggleSwitch(key) {
    document.getElementById("sw-" + key)?.classList.toggle("on");
  }

  function openSheet(id) {
    if (id === "contentsSheet") renderContentsSheet();
    document.getElementById(id)?.classList.add("open");
  }

  function closeSheet(id) {
    document.getElementById(id)?.classList.remove("open");
  }

  function renderContentsSheet() {
    const box = document.getElementById("contentsListContainer");
    box.innerHTML = \`
      <div style="font-size:13px; color:#D99B93; margin-bottom:8px">Bookmarks (\${bookmarks.length})</div>
      \${bookmarks.map(b => \`
        <div class="menu-row" onclick="closeSheet('contentsSheet'); document.getElementById('pdf-page-\${b.page}')?.scrollIntoView({behavior:'smooth'})">
          <span>🔖</span><span style="flex:1">\${b.title}</span><span style="color:#D99B93">p.\${b.page}</span>
        </div>
      \`).join("")}
      <hr style="border:none; border-top:1px solid #312C2C; margin:10px 0" />
      <div style="font-size:13px; color:#D99B93; margin-bottom:8px">Saved Quotes (2)</div>
      <div class="menu-row" style="font-size:13.5px">"INTERCHANGED COEFFICIENTS TRICK: Add both → x + y = 10, Subtract → x - y = 1. Solve in 5 sec!"</div>
      <div class="menu-row" style="font-size:13.5px">"STEP-MARKING SECRET: Writing the 3 vertex coordinates clearly carries 1 full mark out of 3."</div>
    \`;
  }

  function toggleInPdfSearch() {
    const bar = document.getElementById("inPdfSearchBar");
    bar.classList.toggle("open");
    if (bar.classList.contains("open")) document.getElementById("inPdfSearchInput").focus();
  }

  function runInPdfSearch() {
    const q = document.getElementById("inPdfSearchInput").value.trim();
    document.getElementById("inPdfMatchBadge").textContent = q ? \`1/\${activeDoc.pageCount} matches\` : "0/0";
  }

  function startReadAloud() {
    showToast("Read Aloud active: Speaking Page " + activeDoc.currentPage);
    if ("speechSynthesis" in window) {
      window.speechSynthesis.cancel();
      const u = new SpeechSynthesisUtterance("Chapter 3: Pair of Linear Equations in Two Variables. Standard General Form a 1 x plus b 1 y plus c 1 equals 0.");
      window.speechSynthesis.speak(u);
    }
  }

  function startAutoScroll() {
    stopAutoScroll();
    showToast("Auto-scroll started (Tap back to stop)");
    const vp = document.getElementById("readerViewport");
    autoScrollTimer = setInterval(() => { vp.scrollTop += 1.5; }, 20);
  }

  function stopAutoScroll() {
    if (autoScrollTimer) { clearInterval(autoScrollTimer); autoScrollTimer = null; }
  }

  function showFileInfo() {
    alert(\`File Information:\\n\\nTitle: \${activeDoc.fullTitle}\\nFolder: \${activeDoc.folder} (\${activeDoc.volume})\\nSize: \${activeDoc.size}\\nPages: \${activeDoc.pageCount}\`);
  }

  function showInFolderFromMenu() {
    closeReader();
    switchTab("Folders");
    openFolder(activeDoc.folder);
  }

  function deleteCurrentDoc() {
    const idx = documents.findIndex(d => d.id === activeDoc.id);
    if (idx >= 0 && documents.length > 1) {
      documents.splice(idx, 1);
      closeReader();
      renderLibrary();
      showToast("Deleted document");
    }
  }

  function setAppTheme(mode) {
    ["system", "light", "dark"].forEach(m => {
      document.getElementById("appTheme-" + m)?.classList.toggle("active", m === mode);
    });
    document.body.classList.toggle("light-theme", mode === "light");
  }

  function handleFileSelect(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    closeSheet("homeMoreSheet");
    const newDoc = {
      id: Date.now(),
      title: file.name.replace(/\\.pdf$/i, ""),
      fullTitle: file.name,
      date: "Just now",
      size: (file.size / 1024 / 1024).toFixed(2) + " MB",
      folder: "Download",
      volume: "Internal storage",
      author: "Device User",
      pageCount: 10,
      currentPage: 1,
      lastOpened: "Just now",
      progress: 10,
      favorite: false
    };
    documents.unshift(newDoc);
    renderLibrary();
    openReader(newDoc.id);
    showToast("Imported " + file.name);
  }

  function showToast(msg) {
    const box = document.getElementById("toastBox");
    box.textContent = msg;
    box.classList.add("show");
    clearTimeout(window.__toastTimer);
    window.__toastTimer = setTimeout(() => box.classList.remove("show"), 2500);
  }

  renderReadingThemeGrid();
  renderLibrary();
</script>
</body>
</html>`;

// Minimal valid PDF file bytes for /sample.pdf download
const minimalPdfBytes = Buffer.from(
  "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Count 1/Kids[3 0 R]>>endobj\n3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Contents 4 0 R/Resources<</Font<</F1 5 0 R>>>>>>endobj\n4 0 obj<</Length 118>>stream\nBT /F1 16 Tf 50 720 Td (Class 10 Maths Cheat-Sheet - Chapter 3: Pair of Linear Equations) Tj /F1 12 Tf 0 -28 Td (Orion PDF Reader - Pro Unlocked) Tj ET\nendstream\nendobj\n5 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica-Bold>>endobj\nxref\n0 6\n0000000000 65535 f \n0000000009 00000 n \n0000000052 00000 n \n0000000101 00000 n \n0000000206 00000 n \n0000000374 00000 n \ntrailer<</Size 6/Root 1 0 R>>\nstartxref\n445\n%%EOF",
  "utf-8"
);

const server = http.createServer((req, res) => {
  const url = req.url || "/";
  if (url.startsWith("/sample.pdf")) {
    res.writeHead(200, {
      "Content-Type": "application/pdf",
      "Content-Disposition": 'inline; filename="Class_10_Maths_Cheat_Sheet.pdf"'
    });
    res.end(minimalPdfBytes);
    return;
  }

  res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
  res.end(htmlPage);
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`Orion PDF Reader server running on http://0.0.0.0:${PORT}`);
});
