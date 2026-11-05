import React, { useState, useEffect, useRef } from 'react';
import { 
  List, 
  ChevronRight, 
  Calculator, 
  History, 
  Trash2, 
  Copy, 
  ArrowRightLeft, 
  Percent, 
  Users, 
  X,
  Check
} from 'lucide-react';

interface HistoryItem {
  id: number;
  expression: string;
  result: string;
  time: string;
}

export default function App() {
  const [expression, setExpression] = useState('');
  const [displayValue, setDisplayValue] = useState('0');
  const [topDisplay, setTopDisplay] = useState('');
  const [activeOp, setActiveOp] = useState<string | null>(null);
  const [isEvaluated, setIsEvaluated] = useState(false);
  const [isRadian, setIsRadian] = useState(false);
  const [isSciOpen, setIsSciOpen] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [showTools, setShowTools] = useState(false);
  const [toolsTab, setToolsTab] = useState<'convert' | 'discount' | 'split'>('convert');
  const [copied, setCopied] = useState(false);
  const [historyList, setHistoryList] = useState<HistoryItem[]>(() => {
    try {
      const saved = localStorage.getItem('kalkulator_history');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  // Unit Converter State
  const [unitCategory, setUnitCategory] = useState<'length' | 'weight' | 'temp' | 'area' | 'data'>('length');
  const [fromUnit, setFromUnit] = useState('m');
  const [toUnit, setToUnit] = useState('km');
  const [unitInput, setUnitInput] = useState('1');

  // Discount State
  const [discPrice, setDiscPrice] = useState('150000');
  const [discPercent, setDiscPercent] = useState(20);
  const [discExtra, setDiscExtra] = useState(5);
  const [discTax, setDiscTax] = useState(11);

  // Split Bill State
  const [splitBill, setSplitBill] = useState('250000');
  const [splitTip, setSplitTip] = useState(10);
  const [splitPeople, setSplitPeople] = useState(4);

  useEffect(() => {
    try {
      localStorage.setItem('kalkulator_history', JSON.stringify(historyList));
    } catch {}
  }, [historyList]);

  // Keyboard support
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) return;
      if (e.key >= '0' && e.key <= '9') onKeyClick(e.key);
      else if (e.key === '.' || e.key === ',') onKeyClick(',');
      else if (e.key === '+') onKeyClick('+');
      else if (e.key === '-') onKeyClick('−');
      else if (e.key === '*') onKeyClick('×');
      else if (e.key === '/') onKeyClick('÷');
      else if (e.key === 'Enter' || e.key === '=') onKeyClick('=');
      else if (e.key === 'Backspace') backspace();
      else if (e.key === 'Escape') onKeyClick('AC');
      else if (e.key === '%') onKeyClick('%');
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  });

  // Formatting helpers
  const formatNumber = (numStr: string) => {
    if (!numStr || numStr === 'Error') return numStr || '0';
    const isNeg = numStr.startsWith('-') || numStr.startsWith('−');
    const clean = numStr.replace(/^[−-]/, '');
    const [intPart, decPart] = clean.split(/[.,]/);
    const formattedInt = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
    const prefix = isNeg ? '−' : '';
    return decPart !== undefined ? `${prefix}${formattedInt},${decPart}` : `${prefix}${formattedInt}`;
  };

  const formatExpression = (expr: string) => {
    return expr
      .replace(/\*/g, '×')
      .replace(/\//g, '÷')
      .replace(/-/g, '−')
      .replace(/\b(\d+)\b/g, (m) => m.replace(/\B(?=(\d{3})+(?!\d))/g, ' '));
  };

  const evaluateExpression = (expr: string, radMode: boolean): string => {
    try {
      let sanitized = expr
        .replace(/×/g, '*')
        .replace(/÷/g, '/')
        .replace(/−/g, '-')
        .replace(/ /g, '')
        .replace(/,/g, '.');

      // Constants
      sanitized = sanitized.replace(/π/g, `${Math.PI}`).replace(/e/g, `${Math.E}`);

      // Basic functions replace
      sanitized = sanitized.replace(/sqrt\(([^)]+)\)/g, 'Math.sqrt($1)');
      sanitized = sanitized.replace(/log\(([^)]+)\)/g, 'Math.log10($1)');
      sanitized = sanitized.replace(/ln\(([^)]+)\)/g, 'Math.log($1)');

      // Trigo
      if (!radMode) {
        sanitized = sanitized.replace(/sin\(([^)]+)\)/g, 'Math.sin(($1)*Math.PI/180)');
        sanitized = sanitized.replace(/cos\(([^)]+)\)/g, 'Math.cos(($1)*Math.PI/180)');
        sanitized = sanitized.replace(/tan\(([^)]+)\)/g, 'Math.tan(($1)*Math.PI/180)');
      } else {
        sanitized = sanitized.replace(/sin\(([^)]+)\)/g, 'Math.sin($1)');
        sanitized = sanitized.replace(/cos\(([^)]+)\)/g, 'Math.cos($1)');
        sanitized = sanitized.replace(/tan\(([^)]+)\)/g, 'Math.tan($1)');
      }

      // Powers ^
      sanitized = sanitized.replace(/(\d+(?:\.\d+)?)\^(\d+(?:\.\d+)?)/g, 'Math.pow($1,$2)');

      // Evaluate safely
      // eslint-disable-next-line no-eval
      const result = Function(`'use strict'; return (${sanitized})`)();
      if (isNaN(result) || !isFinite(result)) return 'Error';
      const rounded = Math.round(result * 1e10) / 1e10;
      return rounded.toString();
    } catch {
      return 'Error';
    }
  };

  const onKeyClick = (key: string) => {
    if (key === 'AC' || key === 'C') {
      setExpression('');
      setDisplayValue('0');
      setTopDisplay('');
      setActiveOp(null);
      setIsEvaluated(false);
      return;
    }

    if (key === '=') {
      if (!expression) return;
      const res = evaluateExpression(expression, isRadian);
      const formattedRes = formatNumber(res);
      const formattedExpr = formatExpression(expression);

      if (res !== 'Error') {
        const newItem: HistoryItem = {
          id: Date.now(),
          expression: formattedExpr,
          result: formattedRes,
          time: new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })
        };
        setHistoryList((prev) => [newItem, ...prev.slice(0, 49)]);
      }

      setDisplayValue(formattedRes);
      setTopDisplay(formattedExpr);
      setActiveOp(null);
      setIsEvaluated(true);
      return;
    }

    if (key === '+/-') {
      const base = isEvaluated ? displayValue.replace(/ /g, '').replace(',', '.') : expression;
      if (!base || base === '0') return;
      const newExpr = base.startsWith('-') ? base.substring(1) : '-' + base;
      setExpression(newExpr);
      setDisplayValue(formatNumber(newExpr));
      setIsEvaluated(false);
      return;
    }

    if (['+', '−', '×', '÷'].includes(key)) {
      const mathOp = key === '×' ? '*' : key === '÷' ? '/' : key === '−' ? '-' : '+';
      const base = isEvaluated ? displayValue.replace(/ /g, '').replace(',', '.') : expression;
      if (!base) {
        if (key === '−') {
          setExpression('-');
          setActiveOp(key);
        }
        return;
      }
      const last = base.slice(-1);
      const newExpr = ['+', '-', '*', '/'].includes(last) ? base.slice(0, -1) + mathOp : base + mathOp;
      setExpression(newExpr);
      setActiveOp(key);
      setTopDisplay(formatExpression(newExpr));
      setIsEvaluated(false);
      return;
    }

    if (key === ',') {
      const base = isEvaluated ? '0' : expression;
      const parts = base.split(/[+\-*\/]/);
      const current = parts[parts.length - 1] || '';
      if (!current.includes('.')) {
        const newExpr = base + '.';
        setExpression(newExpr);
        setDisplayValue(formatNumber(newExpr));
        setIsEvaluated(false);
      }
      return;
    }

    if (key === '%') {
      const base = isEvaluated ? displayValue.replace(/ /g, '').replace(',', '.') : expression;
      if (!base) return;
      const val = parseFloat(base) / 100;
      const valStr = val.toString();
      setExpression(valStr);
      setDisplayValue(formatNumber(valStr));
      setIsEvaluated(true);
      return;
    }

    // Scientific Functions
    if (['sin', 'cos', 'tan', 'sqrt', 'log', 'ln'].includes(key)) {
      const newExpr = (isEvaluated ? '' : expression) + `${key}(`;
      setExpression(newExpr);
      setTopDisplay(newExpr);
      setIsEvaluated(false);
      return;
    }

    if (key === 'π' || key === 'e') {
      const newExpr = (isEvaluated ? '' : expression) + key;
      setExpression(newExpr);
      setDisplayValue(formatNumber(evaluateExpression(newExpr, isRadian)));
      setIsEvaluated(false);
      return;
    }

    // Digits
    const newExpr = isEvaluated ? key : expression + key;
    setExpression(newExpr);
    setActiveOp(null);
    setIsEvaluated(false);

    // Live preview
    if (newExpr.includes('+') || newExpr.includes('-') || newExpr.includes('*') || newExpr.includes('/')) {
      const live = evaluateExpression(newExpr, isRadian);
      if (live !== 'Error') {
        setDisplayValue(formatNumber(live));
        setTopDisplay(formatExpression(newExpr));
      } else {
        const parts = newExpr.split(/[+\-*\/]/);
        const lastPart = parts[parts.length - 1];
        setDisplayValue(formatNumber(lastPart || '0'));
        setTopDisplay(formatExpression(newExpr));
      }
    } else {
      setDisplayValue(formatNumber(newExpr));
      setTopDisplay('');
    }
  };

  const backspace = () => {
    if (isEvaluated) {
      setIsEvaluated(false);
      setTopDisplay('');
      return;
    }
    if (expression.length > 0) {
      const newExpr = expression.slice(0, -1);
      setExpression(newExpr);
      if (!newExpr) {
        setDisplayValue('0');
        setTopDisplay('');
      } else {
        setDisplayValue(formatNumber(newExpr));
        setTopDisplay(formatExpression(newExpr));
      }
    }
  };

  const copyResult = () => {
    navigator.clipboard.writeText(displayValue);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Unit Converter Calculation
  const unitRatios: Record<string, Record<string, number>> = {
    length: { km: 1000, m: 1, cm: 0.01, mm: 0.001, mi: 1609.34, ft: 0.3048, in: 0.0254 },
    weight: { t: 1000, kg: 1, g: 0.001, mg: 0.000001, lb: 0.453592, oz: 0.0283495 },
    temp: { c: 1, f: 1, k: 1 },
    area: { km2: 1000000, ha: 10000, m2: 1, ft2: 0.092903 },
    data: { tb: 1099511627776, gb: 1073741824, mb: 1048576, kb: 1024, b: 1 }
  };

  const getConvertedUnit = () => {
    const val = parseFloat(unitInput) || 0;
    if (unitCategory === 'temp') {
      if (fromUnit === toUnit) return val.toFixed(2);
      if (fromUnit === 'c' && toUnit === 'f') return (val * 9/5 + 32).toFixed(2);
      if (fromUnit === 'f' && toUnit === 'c') return ((val - 32) * 5/9).toFixed(2);
      if (fromUnit === 'c' && toUnit === 'k') return (val + 273.15).toFixed(2);
      if (fromUnit === 'k' && toUnit === 'c') return (val - 273.15).toFixed(2);
      return val.toFixed(2);
    }
    const cat = unitRatios[unitCategory];
    if (!cat) return '0';
    const base = val * (cat[fromUnit] || 1);
    const converted = base / (cat[toUnit] || 1);
    return Math.round(converted * 100000) / 100000;
  };

  // Discount Calculation
  const originalPrice = parseFloat(discPrice) || 0;
  const firstDiscount = originalPrice * (discPercent / 100);
  const afterFirst = originalPrice - firstDiscount;
  const extraDiscount = afterFirst * (discExtra / 100);
  const afterDiscounts = afterFirst - extraDiscount;
  const taxAmount = afterDiscounts * (discTax / 100);
  const finalPrice = afterDiscounts + taxAmount;
  const totalSavings = originalPrice - afterDiscounts;

  // Split Bill Calculation
  const billAmount = parseFloat(splitBill) || 0;
  const totalTip = billAmount * (splitTip / 100);
  const grandTotal = billAmount + totalTip;
  const perPerson = grandTotal / (splitPeople || 1);

  return (
    <div style={{
      backgroundColor: '#000000',
      color: '#FFFFFF',
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'space-between',
      padding: '20px 24px 30px',
      maxWidth: '480px',
      margin: '0 auto',
      position: 'relative'
    }}>
      {/* Top Header Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <button
          onClick={() => setShowHistory(true)}
          style={{
            background: 'none',
            border: 'none',
            color: '#00E5FF',
            cursor: 'pointer',
            padding: '10px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
          title="Riwayat Perhitungan"
        >
          <List size={28} />
        </button>

        <button
          onClick={() => setIsSciOpen(!isSciOpen)}
          style={{
            backgroundColor: isSciOpen ? 'rgba(0, 229, 255, 0.2)' : '#1E242E',
            border: isSciOpen ? '1px solid #00E5FF' : 'none',
            borderRadius: '16px',
            width: '44px',
            height: '44px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            color: '#00E5FF',
            transition: 'all 0.2s ease'
          }}
          title="Mode Ilmiah"
        >
          <ChevronRight size={24} style={{ transform: isSciOpen ? 'rotate(90deg)' : 'none', transition: 'transform 0.2s' }} />
        </button>
      </div>

      {/* Display Screen */}
      <div 
        onClick={copyResult}
        style={{
          textAlign: 'right',
          marginTop: 'auto',
          marginBottom: '20px',
          padding: '10px 4px',
          cursor: 'pointer'
        }}
        title="Klik untuk menyalin"
      >
        <div style={{
          color: '#A0ABBA',
          fontSize: '28px',
          fontWeight: 400,
          minHeight: '36px',
          overflowX: 'auto',
          whiteSpace: 'nowrap'
        }}>
          {topDisplay}
        </div>
        <div style={{
          color: '#FFFFFF',
          fontSize: displayValue.length > 9 ? '48px' : '64px',
          fontWeight: 300,
          letterSpacing: '-1px',
          lineHeight: 1.1,
          marginTop: '6px'
        }}>
          {displayValue}
        </div>
        {copied && (
          <div style={{ color: '#00E5FF', fontSize: '13px', marginTop: '4px' }}>
            ✓ Disalin ke clipboard
          </div>
        )}
      </div>

      {/* Expandable Scientific Keypad */}
      {isSciOpen && (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(5, 1fr)',
          gap: '8px',
          marginBottom: '14px',
          backgroundColor: '#0A101D',
          padding: '12px',
          borderRadius: '20px',
          border: '1px solid #162438'
        }}>
          <button onClick={() => setIsRadian(!isRadian)} style={sciBtnStyle}>
            {isRadian ? 'RAD' : 'DEG'}
          </button>
          <button onClick={() => onKeyClick('sin')} style={sciBtnStyle}>sin</button>
          <button onClick={() => onKeyClick('cos')} style={sciBtnStyle}>cos</button>
          <button onClick={() => onKeyClick('tan')} style={sciBtnStyle}>tan</button>
          <button onClick={() => onKeyClick('ln')} style={sciBtnStyle}>ln</button>

          <button onClick={() => onKeyClick('log')} style={sciBtnStyle}>log</button>
          <button onClick={() => onKeyClick('sqrt')} style={sciBtnStyle}>√</button>
          <button onClick={() => onKeyClick('π')} style={sciBtnStyle}>π</button>
          <button onClick={() => onKeyClick('e')} style={sciBtnStyle}>e</button>
          <button onClick={() => onKeyClick('(')} style={sciBtnStyle}>(</button>
        </div>
      )}

      {/* 4x5 Circular Keypad Grid matching screenshot */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: '14px',
        justifyItems: 'center'
      }}>
        {/* Row 1 */}
        <button onClick={() => onKeyClick(expression || displayValue !== '0' ? 'C' : 'AC')} style={funcBtnStyle}>
          {expression || displayValue !== '0' ? 'C' : 'AC'}
        </button>
        <button onClick={() => onKeyClick('+/-')} style={funcBtnStyle}>⁺/₋</button>
        <button onClick={() => onKeyClick('%')} style={funcBtnStyle}>%</button>
        <button onClick={() => onKeyClick('÷')} style={activeOp === '÷' ? activeOpBtnStyle : opBtnStyle}>÷</button>

        {/* Row 2 */}
        <button onClick={() => onKeyClick('7')} style={numBtnStyle}>7</button>
        <button onClick={() => onKeyClick('8')} style={numBtnStyle}>8</button>
        <button onClick={() => onKeyClick('9')} style={numBtnStyle}>9</button>
        <button onClick={() => onKeyClick('×')} style={activeOp === '×' ? activeOpBtnStyle : opBtnStyle}>×</button>

        {/* Row 3 */}
        <button onClick={() => onKeyClick('4')} style={numBtnStyle}>4</button>
        <button onClick={() => onKeyClick('5')} style={numBtnStyle}>5</button>
        <button onClick={() => onKeyClick('6')} style={numBtnStyle}>6</button>
        <button onClick={() => onKeyClick('−')} style={activeOp === '−' ? activeOpBtnStyle : opBtnStyle}>−</button>

        {/* Row 4 */}
        <button onClick={() => onKeyClick('1')} style={numBtnStyle}>1</button>
        <button onClick={() => onKeyClick('2')} style={numBtnStyle}>2</button>
        <button onClick={() => onKeyClick('3')} style={numBtnStyle}>3</button>
        <button onClick={() => onKeyClick('+')} style={activeOp === '+' ? activeOpBtnStyle : opBtnStyle}>+</button>

        {/* Row 5 */}
        <button onClick={() => setShowTools(true)} style={numBtnStyle} title="Konverter & Alat Keuangan">
          <Calculator size={30} color="#FFFFFF" />
        </button>
        <button onClick={() => onKeyClick('0')} style={numBtnStyle}>0</button>
        <button onClick={() => onKeyClick(',')} style={numBtnStyle}>,</button>
        <button onClick={() => onKeyClick('=')} style={opBtnStyle}>=</button>
      </div>

      {/* History Modal Drawer */}
      {showHistory && (
        <div style={modalOverlayStyle}>
          <div style={modalContentStyle}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <History size={22} color="#00E5FF" />
                <h3 style={{ fontSize: '18px', fontWeight: 600 }}>Riwayat Perhitungan</h3>
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                {historyList.length > 0 && (
                  <button 
                    onClick={() => setHistoryList([])}
                    style={{ background: 'none', border: 'none', color: '#FF5252', cursor: 'pointer', padding: '4px' }}
                    title="Hapus Semua"
                  >
                    <Trash2 size={20} />
                  </button>
                )}
                <button 
                  onClick={() => setShowHistory(false)}
                  style={{ background: 'none', border: 'none', color: '#A0ABBA', cursor: 'pointer', padding: '4px' }}
                >
                  <X size={22} />
                </button>
              </div>
            </div>

            {historyList.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '40px 20px', color: '#A0ABBA' }}>
                Belum ada riwayat perhitungan tersimpan.
              </div>
            ) : (
              <div style={{ overflowY: 'auto', maxHeight: '380px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {historyList.map((item) => (
                  <div
                    key={item.id}
                    onClick={() => {
                      setExpression(item.result.replace(/ /g, '').replace(',', '.'));
                      setDisplayValue(item.result);
                      setIsEvaluated(true);
                      setShowHistory(false);
                    }}
                    style={{
                      backgroundColor: '#161E2E',
                      padding: '12px 16px',
                      borderRadius: '14px',
                      cursor: 'pointer',
                      border: '1px solid #1F2D44'
                    }}
                  >
                    <div style={{ color: '#A0ABBA', fontSize: '13px', display: 'flex', justifyContent: 'space-between' }}>
                      <span>{item.time}</span>
                      <span>{item.expression}</span>
                    </div>
                    <div style={{ color: '#00E5FF', fontSize: '22px', fontWeight: 600, textAlign: 'right', marginTop: '4px' }}>
                      = {item.result}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Tools Modal Sheet (Unit Converter & Finance) */}
      {showTools && (
        <div style={modalOverlayStyle}>
          <div style={modalContentStyle}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <div style={{ display: 'flex', gap: '12px' }}>
                <button
                  onClick={() => setToolsTab('convert')}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: toolsTab === 'convert' ? '#00E5FF' : '#A0ABBA',
                    fontWeight: toolsTab === 'convert' ? 700 : 400,
                    cursor: 'pointer',
                    fontSize: '15px'
                  }}
                >
                  Konversi Satuan
                </button>
                <button
                  onClick={() => setToolsTab('discount')}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: toolsTab === 'discount' ? '#00E5FF' : '#A0ABBA',
                    fontWeight: toolsTab === 'discount' ? 700 : 400,
                    cursor: 'pointer',
                    fontSize: '15px'
                  }}
                >
                  Diskon
                </button>
                <button
                  onClick={() => setToolsTab('split')}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: toolsTab === 'split' ? '#00E5FF' : '#A0ABBA',
                    fontWeight: toolsTab === 'split' ? 700 : 400,
                    cursor: 'pointer',
                    fontSize: '15px'
                  }}
                >
                  Bagi Tagihan
                </button>
              </div>
              <button onClick={() => setShowTools(false)} style={{ background: 'none', border: 'none', color: '#A0ABBA', cursor: 'pointer' }}>
                <X size={22} />
              </button>
            </div>

            {/* TAB 1: UNIT CONVERTER */}
            {toolsTab === 'convert' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px' }}>
                  {(['length', 'weight', 'temp', 'area', 'data'] as const).map((cat) => (
                    <button
                      key={cat}
                      onClick={() => setUnitCategory(cat)}
                      style={{
                        padding: '6px 14px',
                        borderRadius: '20px',
                        border: 'none',
                        backgroundColor: unitCategory === cat ? 'rgba(0, 229, 255, 0.2)' : '#1E242E',
                        color: unitCategory === cat ? '#00E5FF' : '#A0ABBA',
                        cursor: 'pointer',
                        fontSize: '13px',
                        fontWeight: 600,
                        whiteSpace: 'nowrap'
                      }}
                    >
                      {cat === 'length' ? 'Panjang' : cat === 'weight' ? 'Massa' : cat === 'temp' ? 'Suhu' : cat === 'area' ? 'Luas' : 'Data'}
                    </button>
                  ))}
                </div>

                <div style={{ backgroundColor: '#161E2E', padding: '16px', borderRadius: '16px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <input
                      type="number"
                      value={unitInput}
                      onChange={(e) => setUnitInput(e.target.value)}
                      style={inputStyle}
                      placeholder="0"
                    />
                    <select value={fromUnit} onChange={(e) => setFromUnit(e.target.value)} style={selectStyle}>
                      {unitCategory === 'length' && (
                        <>
                          <option value="km">Kilometer (km)</option>
                          <option value="m">Meter (m)</option>
                          <option value="cm">Sentimeter (cm)</option>
                          <option value="mm">Milimeter (mm)</option>
                          <option value="mi">Mil (mi)</option>
                        </>
                      )}
                      {unitCategory === 'weight' && (
                        <>
                          <option value="kg">Kilogram (kg)</option>
                          <option value="g">Gram (g)</option>
                          <option value="t">Ton (t)</option>
                          <option value="lb">Pound (lb)</option>
                        </>
                      )}
                      {unitCategory === 'temp' && (
                        <>
                          <option value="c">Celsius (°C)</option>
                          <option value="f">Fahrenheit (°F)</option>
                          <option value="k">Kelvin (K)</option>
                        </>
                      )}
                      {unitCategory === 'area' && (
                        <>
                          <option value="m2">Meter² (m²)</option>
                          <option value="km2">Kilometer² (km²)</option>
                          <option value="ha">Hektar (ha)</option>
                        </>
                      )}
                      {unitCategory === 'data' && (
                        <>
                          <option value="gb">Gigabyte (GB)</option>
                          <option value="mb">Megabyte (MB)</option>
                          <option value="tb">Terabyte (TB)</option>
                          <option value="kb">Kilobyte (KB)</option>
                        </>
                      )}
                    </select>
                  </div>

                  <div style={{ textAlign: 'center', margin: '8px 0', color: '#00E5FF' }}>
                    <ArrowRightLeft size={18} style={{ cursor: 'pointer' }} onClick={() => {
                      const temp = fromUnit;
                      setFromUnit(toUnit);
                      setToUnit(temp);
                    }} />
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ fontSize: '24px', fontWeight: 600, color: '#00E5FF' }}>
                      {getConvertedUnit()}
                    </div>
                    <select value={toUnit} onChange={(e) => setToUnit(e.target.value)} style={selectStyle}>
                      {unitCategory === 'length' && (
                        <>
                          <option value="m">Meter (m)</option>
                          <option value="km">Kilometer (km)</option>
                          <option value="cm">Sentimeter (cm)</option>
                          <option value="mm">Milimeter (mm)</option>
                          <option value="mi">Mil (mi)</option>
                        </>
                      )}
                      {unitCategory === 'weight' && (
                        <>
                          <option value="g">Gram (g)</option>
                          <option value="kg">Kilogram (kg)</option>
                          <option value="t">Ton (t)</option>
                          <option value="lb">Pound (lb)</option>
                        </>
                      )}
                      {unitCategory === 'temp' && (
                        <>
                          <option value="f">Fahrenheit (°F)</option>
                          <option value="c">Celsius (°C)</option>
                          <option value="k">Kelvin (K)</option>
                        </>
                      )}
                      {unitCategory === 'area' && (
                        <>
                          <option value="km2">Kilometer² (km²)</option>
                          <option value="m2">Meter² (m²)</option>
                          <option value="ha">Hektar (ha)</option>
                        </>
                      )}
                      {unitCategory === 'data' && (
                        <>
                          <option value="mb">Megabyte (MB)</option>
                          <option value="gb">Gigabyte (GB)</option>
                          <option value="tb">Terabyte (TB)</option>
                          <option value="kb">Kilobyte (KB)</option>
                        </>
                      )}
                    </select>
                  </div>
                </div>
              </div>
            )}

            {/* TAB 2: DISCOUNT */}
            {toolsTab === 'discount' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div>
                  <label style={{ fontSize: '13px', color: '#A0ABBA' }}>Harga Asli (Rp)</label>
                  <input
                    type="number"
                    value={discPrice}
                    onChange={(e) => setDiscPrice(e.target.value)}
                    style={{ ...inputStyle, width: '100%', marginTop: '4px' }}
                  />
                </div>
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#A0ABBA' }}>
                    <span>Diskon: {discPercent}%</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="100"
                    value={discPercent}
                    onChange={(e) => setDiscPercent(Number(e.target.value))}
                    style={{ width: '100%', accentColor: '#00E5FF', marginTop: '4px' }}
                  />
                </div>
                <div style={{ backgroundColor: '#161E2E', padding: '16px', borderRadius: '16px', marginTop: '6px' }}>
                  <div style={{ fontSize: '13px', color: '#A0ABBA' }}>Harga Akhir yang Dibayar:</div>
                  <div style={{ fontSize: '28px', fontWeight: 700, color: '#00E5FF', marginTop: '4px' }}>
                    Rp {Math.round(finalPrice).toLocaleString('id-ID')}
                  </div>
                  <div style={{ fontSize: '14px', color: '#10B981', marginTop: '6px' }}>
                    Hemat: Rp {Math.round(totalSavings).toLocaleString('id-ID')}
                  </div>
                </div>
              </div>
            )}

            {/* TAB 3: SPLIT BILL */}
            {toolsTab === 'split' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div>
                  <label style={{ fontSize: '13px', color: '#A0ABBA' }}>Total Tagihan (Rp)</label>
                  <input
                    type="number"
                    value={splitBill}
                    onChange={(e) => setSplitBill(e.target.value)}
                    style={{ ...inputStyle, width: '100%', marginTop: '4px' }}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '13px', color: '#A0ABBA' }}>Jumlah Orang: {splitPeople}</label>
                  <input
                    type="range"
                    min="1"
                    max="20"
                    value={splitPeople}
                    onChange={(e) => setSplitPeople(Number(e.target.value))}
                    style={{ width: '100%', accentColor: '#00E5FF', marginTop: '4px' }}
                  />
                </div>
                <div style={{ backgroundColor: '#161E2E', padding: '16px', borderRadius: '16px', marginTop: '6px' }}>
                  <div style={{ fontSize: '13px', color: '#A0ABBA' }}>Bayar Per Orang:</div>
                  <div style={{ fontSize: '28px', fontWeight: 700, color: '#00E5FF', marginTop: '4px' }}>
                    Rp {Math.round(perPerson).toLocaleString('id-ID')}
                  </div>
                  <div style={{ fontSize: '13px', color: '#A0ABBA', marginTop: '4px' }}>
                    Total: Rp {Math.round(grandTotal).toLocaleString('id-ID')}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

// Button styles matching screenshot & Neon Blue theme
const baseBtnStyle: React.CSSProperties = {
  width: '74px',
  height: '74px',
  borderRadius: '50%',
  border: 'none',
  fontSize: '32px',
  fontWeight: 400,
  cursor: 'pointer',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  transition: 'transform 0.1s ease, filter 0.1s ease',
  outline: 'none'
};

const numBtnStyle: React.CSSProperties = {
  ...baseBtnStyle,
  backgroundColor: '#262A34',
  color: '#FFFFFF'
};

const funcBtnStyle: React.CSSProperties = {
  ...baseBtnStyle,
  backgroundColor: '#3D4758',
  color: '#FFFFFF'
};

const opBtnStyle: React.CSSProperties = {
  ...baseBtnStyle,
  backgroundColor: '#0091EA',
  color: '#FFFFFF'
};

const activeOpBtnStyle: React.CSSProperties = {
  ...baseBtnStyle,
  backgroundColor: '#E0F7FA',
  color: '#0077B6'
};

const sciBtnStyle: React.CSSProperties = {
  backgroundColor: '#161E2E',
  color: '#00E5FF',
  border: 'none',
  borderRadius: '12px',
  padding: '10px 4px',
  fontSize: '13px',
  fontWeight: 600,
  cursor: 'pointer'
};

const modalOverlayStyle: React.CSSProperties = {
  position: 'fixed',
  top: 0,
  left: 0,
  right: 0,
  bottom: 0,
  backgroundColor: 'rgba(0, 0, 0, 0.75)',
  display: 'flex',
  alignItems: 'flex-end',
  justifyContent: 'center',
  zIndex: 1000
};

const modalContentStyle: React.CSSProperties = {
  backgroundColor: '#0D121B',
  width: '100%',
  maxWidth: '480px',
  borderTopLeftRadius: '24px',
  borderTopRightRadius: '24px',
  padding: '20px 24px 30px',
  borderTop: '1px solid #1E2D44',
  maxHeight: '80vh',
  overflowY: 'auto'
};

const inputStyle: React.CSSProperties = {
  backgroundColor: '#0D121B',
  border: '1px solid #1E2D44',
  borderRadius: '12px',
  color: '#FFFFFF',
  padding: '10px 14px',
  fontSize: '18px',
  fontWeight: 600,
  outline: 'none'
};

const selectStyle: React.CSSProperties = {
  backgroundColor: '#1E242E',
  border: '1px solid #2A364C',
  borderRadius: '12px',
  color: '#00E5FF',
  padding: '10px 12px',
  fontSize: '14px',
  fontWeight: 600,
  cursor: 'pointer',
  outline: 'none'
};
