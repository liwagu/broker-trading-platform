"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

const API_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
const AI_API_URL = `${API_URL}/predictions`;

const SECURITIES = [
  { isin: "US67066G1040", name: "NVIDIA Corp", price: 100.00 },
  { isin: "US0378331005", name: "Apple Inc", price: 200.00 },
  { isin: "US5949181045", name: "Microsoft Corp", price: 35.50 },
];

const PORTFOLIOS = ["portfolio-id-1", "portfolio-id-2", "portfolio-id-3", "portfolio-id-4"];

type Order = {
  id: number;
  portfolioId: string;
  isin: string;
  side: "BUY" | "SELL";
  quantity: number;
  price: number;
  status: "CREATED" | "EXECUTED" | "CANCELLED";
};

type PredictionPoint = {
  date: string;
  predicted_price: number;
  confidence_lower: number;
  confidence_upper: number;
};

type Prediction = {
  isin: string;
  security_name: string;
  current_price: number;
  predictions: PredictionPoint[];
  signal: "BUY" | "SELL" | "HOLD";
  confidence: number;
  trend: string;
  ai_summary: string;
};

export default function TradingPlatform() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [portfolioId, setPortfolioId] = useState(PORTFOLIOS[0]);
  const [isin, setIsin] = useState(SECURITIES[0].isin);
  const [side, setSide] = useState<"BUY" | "SELL">("BUY");
  const [quantity, setQuantity] = useState("10");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [prediction, setPrediction] = useState<Prediction | null>(null);
  const [predictLoading, setPredictLoading] = useState(false);
  const [forecastIsin, setForecastIsin] = useState(SECURITIES[0].isin);


  const createOrder = async () => {
    setLoading(true);
    setMessage("");

    try {
      const response = await fetch(`${API_URL}/orders`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          portfolioId,
          isin,
          side,
          quantity: parseFloat(quantity),
        }),
      });

      const data = await response.json();

      if (response.ok) {
        setOrders([data, ...orders]);
        setMessage(`✅ Order ${data.id} created successfully!`);
        setQuantity("10");
      } else {
        setMessage(`❌ Error: ${data.message}`);
      }
    } catch (error) {
      setMessage(`❌ Error: ${error}`);
    } finally {
      setLoading(false);
    }
  };

  const getOrder = async (orderId: string) => {
    setLoading(true);
    setMessage("");

    try {
      const response = await fetch(`${API_URL}/orders/${orderId}`);
      const data = await response.json();

      if (response.ok) {
        const existingIndex = orders.findIndex(o => o.id === data.id);
        if (existingIndex >= 0) {
          const newOrders = [...orders];
          newOrders[existingIndex] = data;
          setOrders(newOrders);
        } else {
          setOrders([data, ...orders]);
        }
        setMessage(`✅ Order ${orderId} retrieved!`);
      } else {
        setMessage(`❌ Error: ${data.message}`);
      }
    } catch (error) {
      setMessage(`❌ Error: ${error}`);
    } finally {
      setLoading(false);
    }
  };

  const cancelOrder = async (orderId: number) => {
    setLoading(true);
    setMessage("");

    try {
      const response = await fetch(`${API_URL}/orders/${orderId}`, {
        method: "PUT",
      });

      const data = await response.json();

      if (response.ok) {
        const newOrders = orders.map(o => o.id === orderId ? data : o);
        setOrders(newOrders);
        setMessage(`✅ Order ${orderId} cancelled!`);
      } else {
        setMessage(`❌ Error: ${data.message}`);
      }
    } catch (error) {
      setMessage(`❌ Error: ${error}`);
    } finally {
      setLoading(false);
    }
  };

  const fetchPrediction = async (targetIsin: string) => {
    setPredictLoading(true);
    try {
      const response = await fetch(`${AI_API_URL}/${targetIsin}?horizon_days=5`);
      const data = await response.json();
      if (response.ok) {
        setPrediction(data);
      } else {
        setMessage(`❌ Prediction Error: ${data.detail || data.message || "Unable to fetch prediction"}`);
      }
    } catch {
      setMessage(`❌ Prediction service unavailable. Ensure the trading backend can reach the AI service.`);
    } finally {
      setPredictLoading(false);
    }
  };

  const selectedSecurity = SECURITIES.find(s => s.isin === isin);
  const estimatedTotal = selectedSecurity ? (selectedSecurity.price * parseFloat(quantity || "0")).toFixed(2) : "0.00";

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-slate-100 dark:from-slate-950 dark:to-slate-900 p-8">
      <div className="max-w-7xl mx-auto space-y-8">
        <div className="space-y-2">
          <h1 className="text-4xl font-bold tracking-tight">Broker Trading Platform</h1>
          <p className="text-muted-foreground">Execute buy/sell orders and manage your portfolio</p>
        </div>

        <Tabs defaultValue="trade" className="space-y-4">
          <TabsList>
            <TabsTrigger value="trade">Trade</TabsTrigger>
            <TabsTrigger value="orders">Orders</TabsTrigger>
            <TabsTrigger value="ai-forecast">🤖 AI Forecast</TabsTrigger>
            <TabsTrigger value="query">Query Order</TabsTrigger>
          </TabsList>

          <TabsContent value="trade" className="space-y-4">
            <div className="grid gap-6 md:grid-cols-2">
              <Card>
                <CardHeader>
                  <CardTitle>Create Order</CardTitle>
                  <CardDescription>Place a buy or sell order for securities</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label>Portfolio</Label>
                    <Select value={portfolioId} onValueChange={setPortfolioId}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {PORTFOLIOS.map(p => (
                          <SelectItem key={p} value={p}>{p}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label>Security</Label>
                    <Select value={isin} onValueChange={setIsin}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {SECURITIES.map(s => (
                          <SelectItem key={s.isin} value={s.isin}>
                            {s.name} (${s.price})
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label>Order Type</Label>
                    <Select value={side} onValueChange={(v) => setSide(v as "BUY" | "SELL")}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="BUY">BUY</SelectItem>
                        <SelectItem value="SELL">SELL</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label>Quantity</Label>
                    <Input
                      type="number"
                      value={quantity}
                      onChange={(e) => setQuantity(e.target.value)}
                      placeholder="10.00"
                      step="0.01"
                    />
                  </div>

                  <div className="p-4 bg-muted rounded-lg space-y-1">
                    <div className="flex justify-between text-sm">
                      <span>Price per share:</span>
                      <span className="font-medium">${selectedSecurity?.price.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span>Estimated total:</span>
                      <span className="font-bold">${estimatedTotal}</span>
                    </div>
                  </div>

                  <Button
                    onClick={createOrder}
                    disabled={loading || !quantity}
                    className="w-full"
                    size="lg"
                  >
                    {loading ? "Processing..." : `${side} ${quantity || 0} shares`}
                  </Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Status</CardTitle>
                  <CardDescription>Order execution status and messages</CardDescription>
                </CardHeader>
                <CardContent>
                  {message ? (
                    <div className={`p-4 rounded-lg ${message.startsWith("✅") ? "bg-green-50 dark:bg-green-950" : "bg-red-50 dark:bg-red-950"}`}>
                      <p className="text-sm">{message}</p>
                    </div>
                  ) : (
                    <p className="text-sm text-muted-foreground">No recent activity</p>
                  )}

                  <div className="mt-6 space-y-2">
                    <h3 className="font-medium text-sm">Initial Portfolio Settings</h3>
                    <ul className="text-sm text-muted-foreground space-y-1">
                      <li>💰 Buying Power: $5,000.00</li>
                      <li>📊 Initial Inventory: 0 shares</li>
                      <li>⚡ Real-time execution</li>
                    </ul>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="orders">
            <Card>
              <CardHeader>
                <CardTitle>Recent Orders</CardTitle>
                <CardDescription>View and manage your orders</CardDescription>
              </CardHeader>
              <CardContent>
                {orders.length > 0 ? (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>ID</TableHead>
                        <TableHead>Portfolio</TableHead>
                        <TableHead>ISIN</TableHead>
                        <TableHead>Side</TableHead>
                        <TableHead>Quantity</TableHead>
                        <TableHead>Price</TableHead>
                        <TableHead>Total</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Action</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {orders.map((order) => (
                        <TableRow key={order.id}>
                          <TableCell className="font-medium">{order.id}</TableCell>
                          <TableCell>{order.portfolioId}</TableCell>
                          <TableCell className="font-mono text-xs">{order.isin}</TableCell>
                          <TableCell>
                            <Badge variant={order.side === "BUY" ? "default" : "secondary"}>
                              {order.side}
                            </Badge>
                          </TableCell>
                          <TableCell>{order.quantity}</TableCell>
                          <TableCell>${order.price.toFixed(2)}</TableCell>
                          <TableCell>${(order.quantity * order.price).toFixed(2)}</TableCell>
                          <TableCell>
                            <Badge
                              variant={
                                order.status === "CREATED" ? "outline" :
                                order.status === "EXECUTED" ? "default" : "destructive"
                              }
                            >
                              {order.status}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            {order.status === "CREATED" && (
                              <Button
                                size="sm"
                                variant="destructive"
                                onClick={() => cancelOrder(order.id)}
                                disabled={loading}
                              >
                                Cancel
                              </Button>
                            )}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                ) : (
                  <p className="text-center text-muted-foreground py-8">
                    No orders yet. Create your first order in the Trade tab.
                  </p>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="ai-forecast">
            <div className="grid gap-6 md:grid-cols-2">
              <Card>
                <CardHeader>
                  <CardTitle>AI Price Prediction</CardTitle>
                  <CardDescription>Get 5-day forecast powered by Kronos</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label>Select Security</Label>
                    <Select value={forecastIsin} onValueChange={setForecastIsin}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {SECURITIES.map(s => (
                          <SelectItem key={s.isin} value={s.isin}>
                            {s.name} (${s.price})
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <Button
                    onClick={() => fetchPrediction(forecastIsin)}
                    disabled={predictLoading}
                    className="w-full"
                    size="lg"
                  >
                    {predictLoading ? "Analyzing..." : "🔮 Get AI Prediction"}
                  </Button>

                  {prediction && (
                    <div className="space-y-4 mt-6">
                      <div className="p-4 bg-gradient-to-r from-purple-50 to-blue-50 dark:from-purple-950 dark:to-blue-950 rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <span className="font-semibold">AI Signal</span>
                          <Badge
                            variant={prediction.signal === "BUY" ? "default" : prediction.signal === "SELL" ? "destructive" : "secondary"}
                            className="text-lg px-4 py-1"
                          >
                            {prediction.signal}
                          </Badge>
                        </div>
                        <div className="text-sm text-muted-foreground">
                          Confidence: {(prediction.confidence * 100).toFixed(0)}% • Trend: {prediction.trend}
                        </div>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Forecast Details</CardTitle>
                  <CardDescription>AI-powered price predictions</CardDescription>
                </CardHeader>
                <CardContent>
                  {prediction ? (
                    <div className="space-y-4">
                      <div className="p-4 bg-muted rounded-lg">
                        <p className="text-sm leading-relaxed">{prediction.ai_summary}</p>
                      </div>

                      <div className="space-y-2">
                        <h4 className="font-medium text-sm">5-Day Forecast</h4>
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead>Date</TableHead>
                              <TableHead>Price</TableHead>
                              <TableHead>Range</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {prediction.predictions.map((pred, idx) => (
                              <TableRow key={idx}>
                                <TableCell className="font-mono text-xs">{pred.date}</TableCell>
                                <TableCell className="font-bold">${pred.predicted_price.toFixed(2)}</TableCell>
                                <TableCell className="text-xs text-muted-foreground">
                                  ${pred.confidence_lower.toFixed(2)} - ${pred.confidence_upper.toFixed(2)}
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </div>

                      <div className="text-xs text-muted-foreground pt-4 border-t">
                        <p>⚡ Powered by Kronos (Tsinghua University)</p>
                        <p className="mt-1">Model trained on 45+ global exchanges</p>
                      </div>
                    </div>
                  ) : (
                    <p className="text-center text-muted-foreground py-8">
                      Select a security and click &quot;Get AI Prediction&quot; to see the forecast.
                    </p>
                  )}
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="query">
            <Card>
              <CardHeader>
                <CardTitle>Query Order by ID</CardTitle>
                <CardDescription>Retrieve order details by order ID</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex gap-2">
                  <Input
                    id="orderId"
                    type="number"
                    placeholder="Enter order ID"
                  />
                  <Button
                    onClick={() => {
                      const input = document.getElementById("orderId") as HTMLInputElement;
                      if (input.value) getOrder(input.value);
                    }}
                    disabled={loading}
                  >
                    Query
                  </Button>
                </div>

                {orders.length > 0 && (
                  <div className="mt-6">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>ID</TableHead>
                          <TableHead>Portfolio</TableHead>
                          <TableHead>ISIN</TableHead>
                          <TableHead>Side</TableHead>
                          <TableHead>Quantity</TableHead>
                          <TableHead>Price</TableHead>
                          <TableHead>Total</TableHead>
                          <TableHead>Status</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {orders.map((order) => (
                          <TableRow key={order.id}>
                            <TableCell className="font-medium">{order.id}</TableCell>
                            <TableCell>{order.portfolioId}</TableCell>
                            <TableCell className="font-mono text-xs">{order.isin}</TableCell>
                            <TableCell>
                              <Badge variant={order.side === "BUY" ? "default" : "secondary"}>
                                {order.side}
                              </Badge>
                            </TableCell>
                            <TableCell>{order.quantity}</TableCell>
                            <TableCell>${order.price.toFixed(2)}</TableCell>
                            <TableCell>${(order.quantity * order.price).toFixed(2)}</TableCell>
                            <TableCell>
                              <Badge
                                variant={
                                  order.status === "CREATED" ? "outline" :
                                  order.status === "EXECUTED" ? "default" : "destructive"
                                }
                              >
                                {order.status}
                              </Badge>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
