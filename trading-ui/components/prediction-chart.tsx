"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Area, AreaChart } from 'recharts';

type PredictionPoint = {
  date: string;
  predicted_price: number;
  confidence_lower: number;
  confidence_upper: number;
};

type HistoricalData = {
  timestamps: string[];
  prices: number[];
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
  historical_data?: HistoricalData;
};

type PredictionChartProps = {
  prediction: Prediction;
};

export function PredictionChart({ prediction }: PredictionChartProps) {
  // Combine historical and predicted data for the chart
  const chartData = [];

  // Add historical data (last 20 points)
  if (prediction.historical_data) {
    const { timestamps, prices } = prediction.historical_data;
    for (let i = 0; i < timestamps.length; i++) {
      chartData.push({
        date: new Date(timestamps[i]).toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }),
        historical: prices[i],
        predicted: null,
        lower: null,
        upper: null,
        type: 'historical'
      });
    }
  }

  // Add current price as transition point
  chartData.push({
    date: 'Now',
    historical: prediction.current_price,
    predicted: prediction.current_price,
    lower: prediction.current_price,
    upper: prediction.current_price,
    type: 'transition'
  });

  // Add prediction data
  for (const pred of prediction.predictions) {
    chartData.push({
      date: new Date(pred.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
      historical: null,
      predicted: pred.predicted_price,
      lower: pred.confidence_lower,
      upper: pred.confidence_upper,
      type: 'prediction'
    });
  }

  // Calculate price range for Y-axis
  const allPrices = [
    ...(prediction.historical_data?.prices || []),
    prediction.current_price,
    ...prediction.predictions.map(p => p.predicted_price),
    ...prediction.predictions.map(p => p.confidence_lower),
    ...prediction.predictions.map(p => p.confidence_upper),
  ];
  const minPrice = Math.min(...allPrices);
  const maxPrice = Math.max(...allPrices);
  const padding = (maxPrice - minPrice) * 0.1;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Price Forecast Visualization</CardTitle>
        <CardDescription>
          Historical prices (blue) and AI predictions (purple) with confidence intervals
        </CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={400}>
          <AreaChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="colorConfidence" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#8884d8" stopOpacity={0.3}/>
                <stop offset="95%" stopColor="#8884d8" stopOpacity={0}/>
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
            <XAxis
              dataKey="date"
              tick={{ fontSize: 11 }}
              angle={-45}
              textAnchor="end"
              height={80}
            />
            <YAxis
              domain={[minPrice - padding, maxPrice + padding]}
              tick={{ fontSize: 12 }}
              tickFormatter={(value) => `$${value.toFixed(2)}`}
            />
            <Tooltip
              formatter={(value: number) => [`$${value?.toFixed(2)}`, '']}
              labelStyle={{ fontWeight: 'bold' }}
              contentStyle={{ backgroundColor: 'rgba(255, 255, 255, 0.95)', borderRadius: '8px' }}
            />
            <Legend
              wrapperStyle={{ paddingTop: '20px' }}
              iconType="line"
            />

            {/* Confidence interval */}
            <Area
              type="monotone"
              dataKey="upper"
              stroke="none"
              fill="url(#colorConfidence)"
              fillOpacity={0.4}
              name="Confidence Range"
            />
            <Area
              type="monotone"
              dataKey="lower"
              stroke="none"
              fill="url(#colorConfidence)"
              fillOpacity={0.4}
            />

            {/* Historical prices */}
            <Line
              type="monotone"
              dataKey="historical"
              stroke="#3b82f6"
              strokeWidth={2}
              dot={false}
              name="Historical Price"
              connectNulls={false}
            />

            {/* Predicted prices */}
            <Line
              type="monotone"
              dataKey="predicted"
              stroke="#a855f7"
              strokeWidth={2.5}
              strokeDasharray="5 5"
              dot={{ fill: '#a855f7', r: 4 }}
              name="Predicted Price"
              connectNulls={false}
            />
          </AreaChart>
        </ResponsiveContainer>

        <div className="mt-6 grid grid-cols-3 gap-4 text-sm">
          <div className="p-3 bg-blue-50 dark:bg-blue-950 rounded-lg">
            <div className="font-semibold text-blue-700 dark:text-blue-300">Current Price</div>
            <div className="text-2xl font-bold text-blue-900 dark:text-blue-100">
              ${prediction.current_price.toFixed(2)}
            </div>
          </div>

          <div className="p-3 bg-purple-50 dark:bg-purple-950 rounded-lg">
            <div className="font-semibold text-purple-700 dark:text-purple-300">5-Day Target</div>
            <div className="text-2xl font-bold text-purple-900 dark:text-purple-100">
              ${prediction.predictions[prediction.predictions.length - 1]?.predicted_price.toFixed(2)}
            </div>
          </div>

          <div className={`p-3 rounded-lg ${
            prediction.signal === 'BUY' ? 'bg-green-50 dark:bg-green-950' :
            prediction.signal === 'SELL' ? 'bg-red-50 dark:bg-red-950' :
            'bg-gray-50 dark:bg-gray-950'
          }`}>
            <div className={`font-semibold ${
              prediction.signal === 'BUY' ? 'text-green-700 dark:text-green-300' :
              prediction.signal === 'SELL' ? 'text-red-700 dark:text-red-300' :
              'text-gray-700 dark:text-gray-300'
            }`}>AI Signal</div>
            <div className={`text-2xl font-bold ${
              prediction.signal === 'BUY' ? 'text-green-900 dark:text-green-100' :
              prediction.signal === 'SELL' ? 'text-red-900 dark:text-red-100' :
              'text-gray-900 dark:text-gray-100'
            }`}>
              {prediction.signal}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
